package com.codeseven.pos.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentAudioRecordingBinding;
import com.codeseven.pos.helper.WavAudioRecorder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class AudioRecordingFragment extends Fragment {

    FragmentAudioRecordingBinding fragmentAudioRecordingBinding;

    // string variable is created for storing a file name
    private static String mFileName = null;
    private int fileNameCounter= 1;

    // constant for storing audio permission
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    public enum State {INITIALIZING, READY, RECORDING, ERROR, STOPPED};

    public static final boolean RECORDING_UNCOMPRESSED = true;
//    public static final boolean RECORDING_COMPRESSED = false;

    // The interval in which the recorded samples are output to the file
    // Used only in uncompressed mode
    private static final int TIMER_INTERVAL = 120;

    // Recorder used for uncompressed recording
    private AudioRecord audioRecorder = null;

    // Output file path
    private String filePath = null;

    // Recorder state; see State
    private WavAudioRecorder.State state;

    // File writer (only in uncompressed mode)
    private RandomAccessFile randomAccessWriter;


    // Number of channels, sample rate, sample size(size in bits), buffer size, audio source, sample size(see AudioFormat)
    private short nChannels;
    private int sRate;
    private short mBitsPersample;
    private int mBufferSize;
    private int mAudioSource;
    private int aFormat;

    // Number of frames/samples written to file on each output(only in uncompressed mode)
    private int mPeriodInFrames;

    // Buffer for output(only in uncompressed mode)
    private byte[] buffer;

    // Number of bytes written to file after header(only in uncompressed mode)
    // after stop() is called, this size is written to the header/data chunk in the wave file
    private int payloadSize;

    /**
     *
     * Returns the state of the recorder in a WavAudioRecorder.State typed object.
     * Useful, as no exceptions are thrown.
     *
     * @return recorder state
     */
    public WavAudioRecorder.State getState() {
        return state;
    }


    private final static int[] sampleRates = {44100, 22050, 11025, 8000};

    public AudioRecordingFragment() {
        // Required empty public constructor
    }


    public static AudioRecordingFragment newInstance() {
        AudioRecordingFragment fragment = new AudioRecordingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public Context getcontext() {
        return requireContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        fragmentAudioRecordingBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_audio_recording, container, false);
        View view = fragmentAudioRecordingBinding.getRoot();
//
        //Creating recorrder instance...
//        createAudioRecorder(MediaRecorder.AudioSource.MIC, sampleRates[0], AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if(!CheckPermissions())
        {
            RequestPermissions();
        }
        else
        {
            //Creating recorrder instance...
        createAudioRecorder(MediaRecorder.AudioSource.MIC, sampleRates[0], AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        }

        fragmentAudioRecordingBinding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFileName = Environment.getExternalStorageDirectory().getPath();
                mFileName += "/TempAudioFile.wav";
                setOutputFile(mFileName);

                prepare();
                start();
            }
        });

        fragmentAudioRecordingBinding.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
                reset();

                playAudio();

                uploadToGCS();
//                release();
            }
        });

        fragmentAudioRecordingBinding.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                playAudio();
            }
        });


        return view;

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        // this method is called when user will
        // grant the permission for audio recording.
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(requireContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(requireContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(requireActivity(), new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
        //	periodic updates on the progress of the record head
        public void onPeriodicNotification(AudioRecord recorder) {
            if (WavAudioRecorder.State.STOPPED == state) {
                Log.d(requireContext().toString(), "recorder stopped");
                return;
            }
            int numOfBytes = audioRecorder.read(buffer, 0, buffer.length); // read audio data to buffer
//			Log.d(WavAudioRecorder.this.getClass().getName(), state + ":" + numOfBytes);
            try {
                randomAccessWriter.write(buffer);          // write audio data to file
                payloadSize += buffer.length;
            } catch (IOException e) {
                Log.e(WavAudioRecorder.class.getName(), "Error occured in updateListener, recording is aborted");
                e.printStackTrace();
            }
        }

        //	reached a notification marker set by setNotificationMarkerPosition(int)
        public void onMarkerReached(AudioRecord recorder) {
        }
    };

    public void createAudioRecorder(int audioSource, int sampleRate, int channelConfig, int audioFormat) {
        try {
            if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
                mBitsPersample = 16;
            } else {
                mBitsPersample = 8;
            }

            if (channelConfig == AudioFormat.CHANNEL_IN_MONO) {
                nChannels = 1;
            } else {
                nChannels = 2;
            }

            mAudioSource = audioSource;
            sRate = sampleRate;
            aFormat = audioFormat;

            mPeriodInFrames = sampleRate * TIMER_INTERVAL / 1000;        //?
            mBufferSize = mPeriodInFrames * 2 * nChannels * mBitsPersample / 8;        //?
            if (mBufferSize < AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)) {
                // Check to make sure buffer size is not smaller than the smallest allowed one
                mBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                // Set frame period and timer interval accordingly
                mPeriodInFrames = mBufferSize / (2 * mBitsPersample * nChannels / 8);
                Log.w(WavAudioRecorder.class.getName(), "Increasing buffer size to " + Integer.toString(mBufferSize));
            }
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Toast.makeText(requireContext(), "Grant required permissions.", Toast.LENGTH_SHORT).show();
                    RequestPermissions();

                    return;
                }
                audioRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, mBufferSize);

                if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
                    throw new Exception("AudioRecord initialization failed");
                }
                audioRecorder.setRecordPositionUpdateListener(updateListener);
                audioRecorder.setPositionNotificationPeriod(mPeriodInFrames);
                filePath = null;
                state = WavAudioRecorder.State.INITIALIZING;


        mFileName = Environment.getExternalStorageDirectory().getPath();
        mFileName += "/TempAudioFile.wav";
                setOutputFile(mFileName);

        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e(WavAudioRecorder.class.getName(), e.getMessage());
            } else {
                Log.e(WavAudioRecorder.class.getName(), "Unknown error occured while initializing recording");
            }
            state = WavAudioRecorder.State.ERROR;
        }
    }


    public void setOutputFile(String argPath) {
        try {
            if (state == WavAudioRecorder.State.INITIALIZING) {
                filePath = argPath;
            }
        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e(WavAudioRecorder.class.getName(), e.getMessage());
            } else {
                Log.e(WavAudioRecorder.class.getName(), "Unknown error occured while setting output path");
            }
            state = WavAudioRecorder.State.ERROR;
        }
    }


    /**
     *
     * Prepares the recorder for recording, in case the recorder is not in the INITIALIZING state and the file path was not set
     * the recorder is set to the ERROR state, which makes a reconstruction necessary.
     * In case uncompressed recording is toggled, the header of the wave file is written.
     * In case of an exception, the state is changed to ERROR
     *
     */
    public void prepare() {
        try {
            if (state == WavAudioRecorder.State.INITIALIZING) {
                if ((audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) & (filePath != null)) {
                    // write file header
                    randomAccessWriter = new RandomAccessFile(filePath, "rw");
                    randomAccessWriter.setLength(0); // Set file length to 0, to prevent unexpected behavior in case the file already existed
                    randomAccessWriter.writeBytes("RIFF");
                    randomAccessWriter.writeInt(0); // Final file size not known yet, write 0
                    randomAccessWriter.writeBytes("WAVE");
                    randomAccessWriter.writeBytes("fmt ");
                    randomAccessWriter.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
                    randomAccessWriter.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
                    randomAccessWriter.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
                    randomAccessWriter.writeInt(Integer.reverseBytes(sRate)); // Sample rate
                    randomAccessWriter.writeInt(Integer.reverseBytes(sRate * nChannels * mBitsPersample / 8)); // Byte rate, SampleRate*NumberOfChannels*mBitsPersample/8
                    randomAccessWriter.writeShort(Short.reverseBytes((short) (nChannels * mBitsPersample / 8))); // Block align, NumberOfChannels*mBitsPersample/8
                    randomAccessWriter.writeShort(Short.reverseBytes(mBitsPersample)); // Bits per sample
                    randomAccessWriter.writeBytes("data");
                    randomAccessWriter.writeInt(0); // Data chunk size not known yet, write 0
                    buffer = new byte[mPeriodInFrames * mBitsPersample / 8 * nChannels];
                    state = WavAudioRecorder.State.READY;
                } else {
                    Log.e(WavAudioRecorder.class.getName(), "prepare() method called on uninitialized recorder");
                    state = WavAudioRecorder.State.ERROR;
                }
            } else {
                Log.e(WavAudioRecorder.class.getName(), "prepare() method called on illegal state");
                release();
                state = WavAudioRecorder.State.ERROR;
            }
        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e(WavAudioRecorder.class.getName(), e.getMessage());
            } else {
                Log.e(WavAudioRecorder.class.getName(), "Unknown error occured in prepare()");
            }
            state = WavAudioRecorder.State.ERROR;
        }
    }

    /**
     *
     *
     *  Releases the resources associated with this class, and removes the unnecessary files, when necessary
     *
     */
    public void release() {
        if (state == WavAudioRecorder.State.RECORDING) {
            stop();
        } else {
            if (state == WavAudioRecorder.State.READY) {
                try {
                    randomAccessWriter.close(); // Remove prepared file
                } catch (IOException e) {
                    Log.e(WavAudioRecorder.class.getName(), "I/O exception occured while closing output file");
                }
                (new File(filePath)).delete();
            }
        }

        if (audioRecorder != null) {
            audioRecorder.release();
        }
    }

    /**
     *
     *
     * Resets the recorder to the INITIALIZING state, as if it was just created.
     * In case the class was in RECORDING state, the recording is stopped.
     * In case of exceptions the class is set to the ERROR state.
     *
     */
    public void reset() {
        try {
            if (state != WavAudioRecorder.State.ERROR) {
                release();
                filePath = null; // Reset file path
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    RequestPermissions();
                    return;
                }
                audioRecorder = new AudioRecord(mAudioSource, sRate, nChannels, aFormat, mBufferSize);
                if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
                    throw new Exception("AudioRecord initialization failed");
                }
                audioRecorder.setRecordPositionUpdateListener(updateListener);
                audioRecorder.setPositionNotificationPeriod(mPeriodInFrames);
                state = WavAudioRecorder.State.INITIALIZING;
            }
        }catch (Exception e) {
            Log.e(WavAudioRecorder.class.getName(), e.getMessage());
            state = WavAudioRecorder.State.ERROR;
        }
    }

    /**
     *
     *
     * Starts the recording, and sets the state to RECORDING.
     * Call after prepare().
     *
     */
    public void start() {
        if (state == WavAudioRecorder.State.READY) {
            payloadSize = 0;
            audioRecorder.startRecording();
            audioRecorder.read(buffer, 0, buffer.length);	//[TODO: is this necessary]read the existing data in audio hardware, but don't do anything
            state = WavAudioRecorder.State.RECORDING;
        } else {
            Log.e(WavAudioRecorder.class.getName(), "start() called on illegal state");
            state = WavAudioRecorder.State.ERROR;
        }
    }

    /**
     *
     *
     *  Stops the recording, and sets the state to STOPPED.
     * In case of further usage, a reset is needed.
     * Also finalizes the wave file in case of uncompressed recording.
     *
     */
    public void stop() {
        if (state == WavAudioRecorder.State.RECORDING) {
            audioRecorder.stop();
            try {
                randomAccessWriter.seek(4); // Write size to RIFF header
                randomAccessWriter.writeInt(Integer.reverseBytes(36+payloadSize));

                randomAccessWriter.seek(40); // Write size to Subchunk2Size field
                randomAccessWriter.writeInt(Integer.reverseBytes(payloadSize));

                randomAccessWriter.close();
            } catch(IOException e) {
                Log.e(WavAudioRecorder.class.getName(), "I/O exception occured while closing output file");
                state = WavAudioRecorder.State.ERROR;
            }
            state = WavAudioRecorder.State.STOPPED;
        } else {
            Log.e(WavAudioRecorder.class.getName(), "stop() called on illegal state");
            state = WavAudioRecorder.State.ERROR;
        }
    }



    public void playAudio() {
        // for playing our recorded audio
        // we are using media player class.
        mFileName = Environment.getExternalStorageDirectory().getPath();
        mFileName += "/TempAudioFile.wav";

        MediaPlayer  mPlayer = new MediaPlayer();
        try {
            // below method is used to set the
            // data source which will be our file name
            mPlayer.setDataSource(mFileName);

            // below method will prepare our media player
            mPlayer.prepare();

            // below method will start our media player.
            mPlayer.start();
            fragmentAudioRecordingBinding.tvRecordingStatus.setText("Recording Started Playing");
        } catch (IOException e) {
            Log.e("TAG", "prepare() failed");
        }
    }
//
//    public void pauseRecording() {
//        // below method will stop
//        // the audio recording.
//        mRecorder.stop();
//
//        // below method will release
//        // the media recorder class.
//        mRecorder.release();
//
//
//        mRecorder = null;
//        fragmentAudioRecordingBinding.tvRecordingStatus.setText("Recording Stopped");
//    }
//

    public void uploadToGCS()
    {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

        StorageReference storageReference = firebaseStorage.getReference();

        StorageReference audioReference = storageReference.child("TempAudioFile.wav");
        audioReference.getPath();

// Reference's name is the last segment of the full path: "space.jpg"
// This is analogous to the file name
        audioReference.getName();

// Reference's bucket is the name of the storage bucket that the files are stored in
        audioReference.getBucket();

        //////////////////////////////////////////

        UploadTask uploadTask;

        mFileName = Environment.getExternalStorageDirectory().getPath();
        mFileName += "/TempAudioFile.wav";
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(mFileName);

            Uri returnUri = Uri.fromFile(new File(mFileName));
            String mimeType = requireContext().getContentResolver().getType(returnUri);


            uploadTask = audioReference.putStream(inputStream);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mFileName = "";
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mFileName = "";

                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }




    }
}