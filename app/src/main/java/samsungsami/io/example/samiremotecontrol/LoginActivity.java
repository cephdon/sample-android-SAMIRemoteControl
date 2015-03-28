/*
 * Copyright (C) 2015 Samsung Electronics Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package samsungsami.io.example.samiremotecontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.concurrent.ExecutionException;

import io.samsungsami.api.UsersApi;
import io.samsungsami.client.ApiException;
import io.samsungsami.model.UserEnvelope;

public class LoginActivity extends Activity {

    // WebView to login via Samsung SAMI Accounts service
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login();
    }


    /**
     * Setup the webView to show the Samsung SAMI Accounts login webpage
     */
    private void login() {
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            /**
             * Override Url Loading : to catch when the user has logged in and switch to the
             * Welcome activity
             *
             * @param uri : the URL that is going to be load
             * @return URL overriding value (boolean)
             */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String uri) {
                // The login is successful
                if (uri.contains("access_token=")) {
                    // Extracts SAMI access token
                    String[] sArray = uri.split("access_token=");
                    String accessToken = sArray[1];
                    // Save the access token for the next time the application is launched
                    SamiHelper.setAccessToken(accessToken);
                    // Show the welcome screen

                    UsersApi api = new UsersApi();
                    api.setBasePath(SamiHelper.SAMIHUB_BASE_PATH);
                    api.getInvoker().addDefaultHeader("Authorization", "bearer "
                            + SamiHelper.getAccessToken());
                    // If the access token is valid : it will be possible to gather User data from the API
                    try {
                        new CallUsersApiInBackground().execute(api).get();
                    } catch (InterruptedException e) {
                    } catch (ExecutionException e) {
                    }

                    startActivity(new Intent(getApplicationContext(), DeviceListActivity.class));
                    finish();
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, uri);
            }
        });
        // Load the Samsung SAMI Accounts Login webpage
        webView.loadUrl(SamiHelper.getAuthorizationRequestUri());

        // If authentication failed: the Samsung SAMI Accounts Login
        // service will display an explicit error message.
    }

    class CallUsersApiInBackground extends AsyncTask<UsersApi, Void, UserEnvelope> {

        /**
         * Query the API
         *
         * @param apis : Users API
         * @return result (UserEnvelope)
         */
        @Override
        protected UserEnvelope doInBackground(UsersApi... apis) {
            UserEnvelope retVal = null;
            try {
                retVal = apis[0].self();
            } catch (ApiException e) {

            }
            return retVal;
        }

        /**
         * Process the data collected from the API
         *
         * @param result (UserEnvelope)
         */
        @Override
        protected void onPostExecute(UserEnvelope result) {
            // The user full name will be empty when the access token has expired
            if ((result != null) && (result.getData() != null)
                    && (result.getData().getFullName() != null)
                    && (result.getData().getFullName().length() > 0)) {
                SamiHelper.setUserId(result.getData().getId());
            }
        }
    }
}
