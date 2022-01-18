package com.codeseven.pos.util;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
//import androidx.databinding.library.baseAdapters.BR;
//import androidx.databinding.library.baseAdapters.BR;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

//import com.codeseven.pos.BR;
import com.codeseven.pos.BR;
import com.codeseven.pos.api.LoginRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    @Inject
    public LoginObserver loginObserver;

    @Inject
    public LoginViewModel() {
         loginObserver = getUserObserver();
    }
    public LoginObserver getUserObserver()
    {
        return new LoginObserver();
    }

    public static class LoginObserver extends BaseObservable{


        public  MutableLiveData<String> userEmail = new MutableLiveData<>();
        public MutableLiveData<String> userPassword = new MutableLiveData<>();
        @Bindable public MutableLiveData<Boolean> isSuccessful = new MutableLiveData<>();
        @Bindable public MutableLiveData<User>  currentUser = new MutableLiveData<>();


        public MutableLiveData<Boolean> userLoginState = new MutableLiveData<>();
        private LoginRepository loginRepository;
        private MutableLiveData<String> loginResponse;
        LoginPreference loginPreference = new LoginPreference();



        @Inject
        public LoginObserver() {
            userEmail.setValue("waleed.umar@codeninja.pk");
            userPassword.setValue("Admin@321");
            isSuccessful.setValue(false);
            userLoginState.setValue(false);

            loginRepository= new LoginRepository();
            loginResponse = loginRepository.getLoginResponse();

        }
        @Bindable
        public MutableLiveData<String> getUserEmail()
        {
            return userEmail;
        }
        @Bindable
        public MutableLiveData<String> getUserPassword(){
            return  userPassword;
        }
        @Bindable
        public MutableLiveData<Boolean> getIsSuccessful(){
            return isSuccessful;
        }

        @Bindable
        public MutableLiveData<User> getCurrentUser()
        {
            return currentUser;
        }

        @Bindable
        public MutableLiveData<Boolean> getUserLoginState(){ return  userLoginState;}


        public void setUserEmail(String email)
        {
            userEmail.setValue(email);
            notifyPropertyChanged(BR.userEmail);
        }
        public void setUserPassword(String password)
        {
            userPassword.setValue(password);
            notifyPropertyChanged(BR.userPassword);
        }
        public void setIsSuccessful(Boolean istrue)
        {
            isSuccessful.setValue(istrue);
            notifyPropertyChanged(BR.isSuccessful);
        }
        public void setCurrentUser(String email, String password)
        {
            currentUser.setValue(new User(email, password));
            notifyPropertyChanged(BR.currentUser);
        }
        public void setUserLoginState(Boolean isLoggedIn)
        {
            userLoginState.setValue(isLoggedIn);
            notifyPropertyChanged(BR.userLoginState);
        }

        public void verifyLoginInformation()
        {
            if((String.valueOf(getUserEmail().getValue()).length()>0) && (String.valueOf(getUserPassword().getValue()).length()>0))
            {
                loginRepository.generateCustomerTokenByPhone(String.valueOf(getUserEmail().getValue()),String.valueOf(getUserPassword().getValue()));
            }
            else
                loginResponse.postValue("Please fill in the required fields.");
        }

        public MutableLiveData<String> getLoginResponse()
        {
            return loginResponse;
        }
        public void saveLoginData()
        {
            loginPreference.AddLoginPreferences("phone_number",getUserEmail().getValue());
            loginPreference.AddLoginPreferences("password",getUserPassword().getValue());
        }

        public String getPhoneNumberPreference(){
            LoginPreference loginPreference = new LoginPreference();
            return loginPreference.GetLoginPreference("phone_number");
        }

        public String getPasswordPreference(){
            LoginPreference loginPreference = new LoginPreference();
            return loginPreference.GetLoginPreference("password");
        }
        public void savePreferenceLoginState(){
            loginPreference.SetKeepLoggedIn(getUserLoginState().getValue());
        }

        public boolean getPreferenceLoginState()
        {
            return loginPreference.getKeepLoggedIn();
        }



    }


}
