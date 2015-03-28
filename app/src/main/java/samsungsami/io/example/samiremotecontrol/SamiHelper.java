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

/**
 * Helper class to setup SAMI API query
 */
public class SamiHelper {

    // Client identity constants
    public static final String SAMI_REMOTE_CONTROL_PREFERENCES = "SamiRemoteControlPrefs";
    public static final String SAMIHUB_BASE_PATH = "https://api.samsungsami.io/v1.1";
    public static final String SAMI_AUTH_BASE_URL = "https://accounts.samsungsami.io";
    public static final String CLIENT_ID = "YOUR CLIENT APP ID";

    // The current access token
    private static String accessToken = null;

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        SamiHelper.userId = userId;
    }

    private static String userId = null;


    /**
     * Get the access token
     *
     * @return accessToken : the access token
     */
    public static String getAccessToken() {
        return accessToken;
    }

    /**
     * Set the access token
     *
     * @param accessToken : the access token
     */
    public static void setAccessToken(String accessToken) {
        SamiHelper.accessToken = accessToken;
    }

    /**
     * Get the Authorization Request URL for Samsung SAMI Accounts login
     *
     * @return url : Authorization Request URL for Samsung SAMI Accounts login
     */
    public static String getAuthorizationRequestUri() {
        return SAMI_AUTH_BASE_URL + "/authorize?client=mobile&response_type=token&client_id=" + CLIENT_ID;
    }

}
