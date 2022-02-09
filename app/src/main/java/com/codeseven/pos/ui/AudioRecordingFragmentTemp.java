package com.codeseven.pos.ui;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentAudioRecordingBinding;
import com.codeseven.pos.helper.WavAudioRecorder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.waves.util.Horizon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class AudioRecordingFragmentTemp extends Fragment {

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

    ProgressDialog progressDialog;
    public WavAudioRecorder.State getState() {
        return state;
    }


    private final static int[] sampleRates = {44100, 22050, 11025, 8000};

    private Horizon mHorizon;

    private LottieAnimationView animationView;

    public AudioRecordingFragmentTemp() {
        // Required empty public constructor
    }


    public static AudioRecordingFragment newInstance() {
        AudioRecordingFragment fragment = new AudioRecordingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(requireActivity());
        animationView = new LottieAnimationView(requireContext());
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
        mHorizon = new Horizon(fragmentAudioRecordingBinding.glSurface, getResources().getColor(R.color.black),
                48000, 1, 16);

        if(!CheckPermissions())
        {
            RequestPermissions();
        }
        else
        {
            createAudioRecorder(MediaRecorder.AudioSource.MIC, 48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        }

        fragmentAudioRecordingBinding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFileName = Environment.getExternalStorageDirectory().getPath();
                mFileName += "/order"+String.valueOf(System.currentTimeMillis())+".wav";

                setOutputFile(mFileName);

                if(audioRecorder!=null && (audioRecorder.getState() == 0) )
                {
                    createAudioRecorder(MediaRecorder.AudioSource.MIC, 48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                }
                if(audioRecorder == null)
                {
                    createAudioRecorder(MediaRecorder.AudioSource.MIC, 48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                }


                prepare();
                start();
                fragmentAudioRecordingBinding.btnStart.setVisibility(View.GONE);
                fragmentAudioRecordingBinding.tvAudioMessage.setVisibility(View.VISIBLE);
                fragmentAudioRecordingBinding.btnStop.setVisibility(View.VISIBLE);

            }
        });

        fragmentAudioRecordingBinding.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
                reset();
                release();

                fragmentAudioRecordingBinding.btnStop.setVisibility(View.GONE);
                fragmentAudioRecordingBinding.tvAudioMessage.setVisibility(View.GONE);
                fragmentAudioRecordingBinding.btnStart.setVisibility(View.VISIBLE);

                uploadToGCS();
            }
        });



        fragmentAudioRecordingBinding.audioToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.dismissDialog();
                NavHostFragment.findNavController(AudioRecordingFragmentTemp.this).popBackStack();
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
        ActivityCompat.requestPermissions(requireActivity(), new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
        public void onPeriodicNotification(AudioRecord recorder) {
            if (WavAudioRecorder.State.STOPPED == state) {
                Log.d(requireContext().toString(), "recorder stopped");
                return;
            }
            int numOfBytes = audioRecorder.read(buffer, 0, buffer.length); // read audio data to buffer
            try {
                randomAccessWriter.write(buffer);          // write audio data to file
                payloadSize += buffer.length;
            } catch (IOException e) {
                Log.e("Audio Recorder", "Error occured in updateListener, recording is aborted");
                e.printStackTrace();
            }

            mHorizon.updateView(buffer);
        }
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

//            if (channelConfig == AudioFormat.CHANNEL_IN_MONO) {
//                nChannels = 1;
//            } else {
//                nChannels = 2;
//            }

            nChannels = 1;

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
                Log.w("Audio Recorder", "Increasing buffer size to " + Integer.toString(mBufferSize));
            }
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

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

            setOutputFile(mFileName);

        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e("Audio Recorder", e.getMessage());
            } else {
                Log.e("Audio Recorder", "Unknown error occured while initializing recording");
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
                Log.e("Audio Recorder", e.getMessage());
            } else {
                Log.e("Audio Recorder", "Unknown error occured while setting output path");
            }
            state = WavAudioRecorder.State.ERROR;
        }
    }


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
                    Log.e("Audio Recorder", "prepare() method called on uninitialized recorder");
                    state = WavAudioRecorder.State.ERROR;
                }
            } else {
                Log.e("Audio Recorder", "prepare() method called on illegal state");
                release();
                state = WavAudioRecorder.State.ERROR;
            }
        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e("Audio Recorder", e.getMessage());
            } else {
                Log.e("Audio Recorder", "Unknown error occured in prepare()");
            }
            state = WavAudioRecorder.State.ERROR;
        }
    }
    public void release() {
        if (state == WavAudioRecorder.State.RECORDING) {
            stop();
        } else {
            if (state == WavAudioRecorder.State.READY) {
                try {
                    randomAccessWriter.close(); // Remove prepared file
                } catch (IOException e) {
                    Log.e("Audio Recorder", "I/O exception occured while closing output file");
                }
                (new File(filePath)).delete();
            }
        }

        if (audioRecorder != null) {
            audioRecorder.release();
        }
    }

    public void reset() {
        try {
            if (state != WavAudioRecorder.State.ERROR) {
                release();
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
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
            Log.e("Audio Recorder", e.getMessage());
            state = WavAudioRecorder.State.ERROR;
        }
    }

    public void start() {
        if (state == WavAudioRecorder.State.READY) {
            payloadSize = 0;
            audioRecorder.startRecording();
//            audioRecorder.getAudioSessionId();
            audioRecorder.read(buffer, 0, buffer.length);	//[TODO: is this necessary]read the existing data in audio hardware, but don't do anything
            state = WavAudioRecorder.State.RECORDING;
//            animationView.setAnimation(R.raw.speak_now_anim);
//            animationView.cancelAnimation();
//            animationView.playAnimation();

            fragmentAudioRecordingBinding.animationView.playAnimation();
//            fragmentAudioRecordingBinding.listeningDots.playAnimation();

            fragmentAudioRecordingBinding.chronometer.setFormat("%s");

            fragmentAudioRecordingBinding.chronometer.setBase(SystemClock.elapsedRealtime());// set the format for a chronometer
            fragmentAudioRecordingBinding.chronometer.start();

        } else {
            Log.e("Audio Recorder", "start() called on illegal state");
            state = WavAudioRecorder.State.ERROR;
        }
    }

    public void stop() {
        if (state == WavAudioRecorder.State.RECORDING) {
            audioRecorder.stop();
//            animationView.pauseAnimation();
            fragmentAudioRecordingBinding.animationView.pauseAnimation();
//            fragmentAudioRecordingBinding.listeningDots.pauseAnimation();
            fragmentAudioRecordingBinding.chronometer.stop();
            try {
                randomAccessWriter.seek(4); // Write size to RIFF header
                randomAccessWriter.writeInt(Integer.reverseBytes(36+payloadSize));

                randomAccessWriter.seek(40); // Write size to Subchunk2Size field
                randomAccessWriter.writeInt(Integer.reverseBytes(payloadSize));

                randomAccessWriter.close();
            } catch(IOException e) {
                Log.e("Audio Recorder", "I/O exception occured while closing output file");
                state = WavAudioRecorder.State.ERROR;
            }
            state = WavAudioRecorder.State.STOPPED;
        } else {
            Log.e("Audio Recorder", "stop() called on illegal state");
            state = WavAudioRecorder.State.ERROR;
        }
    }

    public void uploadToGCS()
    {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

        StorageReference storageReference = firebaseStorage.getReference();

        StorageReference audioReference = storageReference.child("datasets/"+"order"+String.valueOf(System.currentTimeMillis())+".wav");
        audioReference.getPath();
        audioReference.getName();

        audioReference.getBucket();
        UploadTask uploadTask;

        InputStream inputStream;
        try {
            inputStream = new FileInputStream(mFileName);

            uploadTask = audioReference.putStream(inputStream);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mFileName = "";
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String audio_ref = taskSnapshot.getMetadata().getReference().toString();
                    (new File(filePath)).delete();
                    filePath = null; // Reset file path
//                    progressDialog.dismissDialog();
//                    Toast.makeText(,"Order has been placed",Toast.LENGTH_SHORT).show();
//                    NlpApiCall(audio_ref);

                }
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setMessage("    آپ کا آرڈر بھیج دیا گیا ہے    ")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            NavHostFragment.findNavController(AudioRecordingFragmentTemp.this).popBackStack();
                        }
                    });
            AlertDialog alert = builder.create();

            alert.show();
            TextView textView = (TextView) alert.findViewById(android.R.id.message);
            textView.setTextSize(20);
            textView.setGravity(Gravity.CENTER);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }



    public void NlpApiCall(String audioFileGcp)
    {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(requireContext());

        String url_ = "https://nlp-iz36ezwfjq-uc.a.run.app/predict/"+ audioFileGcp;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String hjj= "[[{\"Quantity\": [Five kilos], \"item Name\": [\"rice\"], \"Date\": [], \"Time\": []}, {\"Quantity\": [\"Five kilos\"], \"item Name\": [\"rice\"], \"Date\": [], \"Time\": []}],[0.8462377190589905]]";
                Log.e("NLP response",response);
                progressDialog.dismissDialog();

//                Snackbar snackbar = Snackbar
//                        .make(fragmentAudioRecordingBinding.getRoot(), response, Snackbar.LENGTH_INDEFINITE);
//                snackbar.show();
                try {
                    JSONArray jsonArray_ = new JSONArray(response);
                    int length = jsonArray_.length();

                    String itemName= jsonArray_.getJSONObject(0).getString("item Name");
                    if(itemName.length()>0) {
                        Toast.makeText(requireContext(), itemName, Toast.LENGTH_LONG).show();
                        Bundle bundle = new Bundle();
                        bundle.putString("itemName", itemName);
                        NavHostFragment.findNavController(AudioRecordingFragmentTemp.this).navigate(R.id.action_audioRecordingFragment_to_shortListedItemsFragment, bundle);
                    }
                    else
                    {
                        Toast.makeText(requireContext(), "Please speak clearly.", Toast.LENGTH_LONG).show();

                    }

//                    JSONObject jsonObject = new JSONObject();
//                    JSONArray jArray = jsonObject.getJSONArray(response);
//                    String responseThis ;
//                    for(int i =0; i <response.length() ; i++){
//                        responseThis = response;
//
//                        Character ab = responseThis.charAt(i);
//
//                        if(!ab.toString().equals("{"))
//                        {
//                            responseThis = response.substring(1,response.length()-1);
//                        }
//                        else
//                        {
//
//                        }
//                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.e("NLP response",error.toString()));

        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        MyRequestQueue.add(stringRequest);
    }

}