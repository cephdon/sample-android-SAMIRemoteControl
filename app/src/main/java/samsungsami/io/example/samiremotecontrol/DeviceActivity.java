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
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.samsungsami.api.DeviceTypesApi;
import io.samsungsami.api.MessagesApi;
import io.samsungsami.client.ApiException;
import io.samsungsami.model.ManifestPropertiesEnvelope;
import io.samsungsami.model.Message;
import io.samsungsami.model.MessageIDEnvelope;
import io.samsungsami.model.NormalizedMessagesEnvelope;


public class DeviceActivity extends Activity {
    // ArrayList to store the device properties
    ArrayList<String> fields = new ArrayList<String>();
    ArrayList<String> values = new ArrayList<String>();
    ArrayList<String> units = new ArrayList<String>();

    // Message stencil
    Message sendCommandMessage = null;

    // Device identifier
    String did;

    // Device type identifier
    String dtid;

    // Manifest version
    BigDecimal manifestVersion;

    // TableLayout for displaying the device current properties
    TableLayout propertiesTableLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        // Retrieve the device identifiers
        dtid = getIntent().getStringExtra("dtid");
        did = getIntent().getStringExtra("did");
        manifestVersion = (BigDecimal) (getIntent().getExtras().get("manifestVersion"));

        // Retrieve the table layout
        propertiesTableLayout = (TableLayout) findViewById(R.id.tableLayout);

        // Display the device name and the device type name
        TextView textView;
        textView = (TextView) findViewById(R.id.deviceNameTextView);
        textView.setText(getIntent().getStringExtra("deviceName"));
        textView = (TextView) findViewById(R.id.deviceTypeNameTextView);
        textView.setText(getIntent().getStringExtra("deviceTypeName"));
        textView = (TextView) findViewById(R.id.action_header);
        textView = (TextView) findViewById(R.id.status_header);

        // Setup the back button
        Button backButton = (Button)findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DeviceListActivity.class));
                finish();
            }
        });

        // Download the Manifest
        DeviceTypesApi api = new DeviceTypesApi();
        api.setBasePath(SamiHelper.SAMIHUB_BASE_PATH);
        api.getInvoker().addDefaultHeader("Authorization", "bearer " + SamiHelper.getAccessToken());
        new CallDeviceTypesApiInBackground().execute(api);
    }

    /**
     * Fill the device status and info table
     */
    void fillTable() {

        TableRow row;
        TextView t1, t2, t3;
        for (int current = 0; current < fields.size(); current++) {
            row = new TableRow(this);
            t1 = new TextView(this);
            t1.setTextColor(Color.GRAY);
            t2 = new TextView(this);
            t2.setTextColor(Color.BLACK);
            t3 = new TextView(this);
            t3.setTextColor(Color.BLACK);

            t1.setText(fields.get(current));
            t2.setText(values.get(current));
            t3.setText(units.get(current));


            t1.setTextSize(15);
            t2.setTextSize(15);
            t3.setTextSize(15);

            row.addView(t1);

            if (fields.get(current).equals("state")) {
                Switch toggle = new Switch(this);
                toggle.setChecked(values.get(current).toLowerCase().equals("on"));
                toggle.setEnabled(false);
                row.addView(toggle);
            } else
                row.addView(t2);

            row.addView(t3);
            propertiesTableLayout.addView(row, new TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            // Adjust the view
            propertiesTableLayout.setColumnStretchable(0, true);
            propertiesTableLayout.setColumnStretchable(1, true);
            propertiesTableLayout.setColumnStretchable(2, true);
        }
    }

    /**
     * AsyncTask to retrieve the DeviceType Manifest of the current device
     * in the DeviceTypesApi API request
     */
    class CallDeviceTypesApiInBackground extends android.os.AsyncTask<DeviceTypesApi, Void, ManifestPropertiesEnvelope> {

        /**
         * Query the DeviceTypesApi
         *
         * @param apis : DeviceTypesApi
         * @return ManifestPropertiesEnvelope
         */
        @Override
        protected ManifestPropertiesEnvelope doInBackground(DeviceTypesApi... apis) {
            ManifestPropertiesEnvelope reval = null;
            try {
                reval = apis[0].getManifestProperties(dtid, manifestVersion.toString());
            } catch (ApiException e) {
            }
            return reval;
        }

        /**
         * Process the data collected from the API
         *
         * @param result (ManifestPropertiesEnvelope)
         */
        @Override
        protected void onPostExecute(ManifestPropertiesEnvelope result) {
            // All fields properties
            HashMap fieldsProperties = (LinkedHashMap) result.getData().getProperties().get("fields");

            // Current field properties
            HashMap fieldProperties;
            String unit;
            for (Object key : fieldsProperties.keySet()) {
                // Add the current field
                fields.add(key.toString());
                // Extract current field properties
                fieldProperties = (HashMap) fieldsProperties.get(key);

                // Add the unit
                try {
                    unit = fieldProperties.get("unit").toString();
                    units.add(unit);

                } catch (Exception e) {
                    units.add("");
                }
            }

            // Query the message API to retrieve all the current values for the fields
            MessagesApi msgApi = new MessagesApi();
            msgApi.setBasePath(SamiHelper.SAMIHUB_BASE_PATH);
            msgApi.getInvoker().addDefaultHeader("Authorization", "bearer " + SamiHelper.getAccessToken());
            new CallMessageApiInBackground().execute(msgApi);
        }
    }

    /**
     * AsyncTask to retrieve the values of the current device for all its fields
     * in the MessageAPI request
     */
    class CallMessageApiInBackground extends android.os.AsyncTask<MessagesApi, Void, List<NormalizedMessagesEnvelope>> {

        /**
         * Query the Messages API
         *
         * @param apis : MessagesAPI
         * @return NormalizedMessagesEnvelope
         */
        @Override
        protected List<NormalizedMessagesEnvelope> doInBackground(MessagesApi... apis) {
            // For all the DeviceType manifest field, get the corresponding value
            // for the current device
            List<NormalizedMessagesEnvelope> results = new ArrayList<NormalizedMessagesEnvelope>();
            for (String field : fields)
                try {
                    results.add(apis[0].getLastNormalizedMessages(1, did, field));
                } catch (ApiException e) {
                    // No value are available
                    results.add(null);
                }

            return results;
        }

        /**
         * Process the data collected from the API
         *
         * @param results
         */
        @Override
        protected void onPostExecute(List<NormalizedMessagesEnvelope> results) {
            // For all the field, we fill the values array.
            for (int i = 0; i < fields.size(); i++)
                try {
                    values.add(results.get(i).getData().get(0).getData().get(fields.get(i)).toString());
                } catch (Exception e) {
                    values.add("");
                }

            // Finally, display the device status and info table
            fillTable();
            addFallback();
        }

    }

    public void addFallback() {
        final ObjectMapper mapper = new ObjectMapper();
        TextView textView = new TextView(this);
        textView.setText("fallback");
        LinearLayout gridLayout = (LinearLayout) findViewById(R.id.gridLayout);

        gridLayout.addView(textView);

        final EditText editText;
        editText = new EditText(this);
        Button button = new Button(this);
        button.setText("send");
        button.setTextAppearance(this, android.R.style.TextAppearance_Small);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                MessagesApi msgApi = new MessagesApi();
                msgApi.setBasePath(SamiHelper.SAMIHUB_BASE_PATH);
                msgApi.getInvoker().addDefaultHeader("Authorization", "bearer " + SamiHelper.getAccessToken());

                sendCommandMessage = new Message();
                sendCommandMessage.setSdid(did);
                sendCommandMessage.setDdid(did);
                sendCommandMessage.setToken("fakeToken");

                try {
                    Map<String, Object> messageData = mapper.readValue(editText.getText().toString(), Map.class);
                    sendCommandMessage.setData(messageData);
                    new CallPostMessageApiInBackground().execute(msgApi);

                } catch (IOException e) {
                }


            }
        });
        gridLayout.addView(editText);
        gridLayout.addView(button);
    }


    class CallPostMessageApiInBackground extends android.os.AsyncTask<MessagesApi, Void, MessageIDEnvelope> {
        @Override
        protected MessageIDEnvelope doInBackground(MessagesApi... apis) {
            MessageIDEnvelope res = null;
            try {
                apis[0].postMessage(sendCommandMessage);
            } catch (ApiException e) {
            }
            return res;
        }

        protected void onPostExecute(MessageIDEnvelope result) {
        }
    }

}
