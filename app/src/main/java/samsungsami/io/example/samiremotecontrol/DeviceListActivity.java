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
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import io.samsungsami.api.UsersApi;
import io.samsungsami.client.ApiException;
import io.samsungsami.model.Device;
import io.samsungsami.model.DeviceType;
import io.samsungsami.model.DeviceTypesEnvelope;
import io.samsungsami.model.DevicesEnvelope;

public class DeviceListActivity extends Activity {

    private ListView mainListView;
    private ArrayList<String> deviceNames = new ArrayList<String>();
    private ArrayList<Device> devices = new ArrayList<Device>();
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        mainListView = (ListView) findViewById( R.id.mainListView );
        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, deviceNames);
        // Setup Users API
        UsersApi api = new UsersApi();
        api.setBasePath(SamiHelper.SAMIHUB_BASE_PATH);
        api.getInvoker().addDefaultHeader("Authorization", "bearer " + SamiHelper.getAccessToken());
        new CallUsersApiInBackground().execute(api);


    }
    /**
     * AsyncTask to retrieve the user devices and devices types from the data collected
     * in the User API request
     */
    class CallUsersApiInBackground extends android.os.AsyncTask<UsersApi, Void, Object[]> {

        /**
         * Query the Users API
         *
         * @param apis : Users API
         * @return Object[] (Object[0] -> DevicesEnvelope and Object[1] -> DeviceTypesEnvelope)
         */
        @Override
        protected Object[] doInBackground(UsersApi... apis) {
            Object[] retVal = new Object[2];
            try {
                retVal[0] = apis[0].getUserDevices(0, 100, true, SamiHelper.getUserId());
                retVal[1] = apis[0].getUserDeviceTypes(0, 100, true, SamiHelper.getUserId());

                // For the moment, all devices types for which the user has an access are gathered.
                // In the future, according to the number of the user, it would be more
                // appropriate to make separate request per Device Type Id to find their
                // names.

            } catch (ApiException e) {
              Log.e("DeviceList", e.toString());
            }
            return retVal;
        }

        /**
         * Process the data collected from the API
         *
         * @param results (Object[0] -> DevicesEnvelope and Object[1] -> DeviceTypesEnvelope)
         */
        @Override
        protected void onPostExecute(Object[] results) {
            DevicesEnvelope devicesEnvelope = (DevicesEnvelope) results[0];

            if (devicesEnvelope.getData().getDevices().size() == 0)
                return;

            for (Device device : devicesEnvelope.getData().getDevices())
            {
                listAdapter.add(device.getName());
                devices.add(device);
            }

            HashSet<String> devicesTypeIds = new HashSet<String>();
            // Collect all devices types ID
            for (Device device : devicesEnvelope.getData().getDevices())
                devicesTypeIds.add(device.getDtid());

            // Find the corresponding device type names
            DeviceTypesEnvelope deviceTypesEnvelope = (DeviceTypesEnvelope) results[1];
            final HashMap<String, String> deviceTypeIdToName = new HashMap<String, String>();
            for (DeviceType deviceType : deviceTypesEnvelope.getData().getDeviceTypes())
                if (devicesTypeIds.contains(deviceType.getId())) {
                    deviceTypeIdToName.put(deviceType.getId(), deviceType.getName());
                }

            mainListView.setAdapter(listAdapter);
            mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                    Device device = devices.get(position);

                    // Prepare the next activity
                    Intent intent = new Intent(getApplicationContext(), DeviceActivity.class);
                    intent.putExtra("did", device.getId());
                    intent.putExtra("dtid", device.getDtid());
                    intent.putExtra("manifestVersion", device.getManifestVersion());
                    intent.putExtra("deviceName", device.getName());
                    intent.putExtra("deviceTypeName", deviceTypeIdToName.get(device.getDtid()));

                    // Starts the device activity to display device info and status
                    startActivity(intent);
                }
            });
        }
    }
}
