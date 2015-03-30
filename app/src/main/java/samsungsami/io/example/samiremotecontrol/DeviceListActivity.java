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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import io.samsungsami.api.DeviceTypesApi;
import io.samsungsami.api.UsersApi;
import io.samsungsami.client.ApiException;
import io.samsungsami.model.Device;
import io.samsungsami.model.DeviceType;
import io.samsungsami.model.DeviceTypeEnvelope;
import io.samsungsami.model.DevicesEnvelope;

public class DeviceListActivity extends Activity {

    private DeviceTypesApi deviceTypesApi;

    private ArrayList<String> deviceNames = new ArrayList<String>();
    private ArrayList<Device> devices = new ArrayList<Device>();
    private HashSet<String> devicesTypeIds = new HashSet<String>();
    private ArrayAdapter<String> listAdapter;

    private ListView mainListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        mainListView = (ListView) findViewById( R.id.mainListView );
        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, deviceNames);

        // Setup Users API
        UsersApi usersApi = new UsersApi();
        usersApi.setBasePath(SamiHelper.SAMIHUB_BASE_PATH);
        usersApi.getInvoker().addDefaultHeader("Authorization", "bearer " + SamiHelper.getAccessToken());

        // Setup DeviceTypes API
        deviceTypesApi = new DeviceTypesApi();
        deviceTypesApi.setBasePath(SamiHelper.SAMIHUB_BASE_PATH);
        deviceTypesApi.getInvoker().addDefaultHeader("Authorization", "bearer " + SamiHelper.getAccessToken());

        // Call Users API
        new CallUsersApiInBackground().execute(usersApi);
    }
    /**
     * AsyncTask to retrieve the user devices and devices types from the data collected
     * in the User API request
     */
    class CallUsersApiInBackground extends android.os.AsyncTask<UsersApi, Void, DevicesEnvelope> {

        /**
         * Query the Users API
         *
         * @param apis : Users API
         */
        @Override
        protected DevicesEnvelope doInBackground(UsersApi... apis) {
            DevicesEnvelope result = null;
            try {
                result = apis[0].getUserDevices(0, 100, true, SamiHelper.getUserId());

            } catch (ApiException e) {
              Log.d("DeviceList", e.toString());
            }
            return result;
        }

        /**
         * Process the data collected from the API
         *
         * @param devicesEnvelope
         */
        @Override
        protected void onPostExecute(DevicesEnvelope devicesEnvelope) {

            if (devicesEnvelope == null)
                // No permission to any DeviceType.
                return;

            if (devicesEnvelope.getData().getDevices().size() == 0)
                return;

            for (Device device : devicesEnvelope.getData().getDevices())
            {
                listAdapter.add(device.getName());
                devices.add(device);
            }

            // Collect all devices types ID
            for (Device device : devicesEnvelope.getData().getDevices())
                devicesTypeIds.add(device.getDtid());

            new CallDeviceTypesApiInBackground().execute(deviceTypesApi);

        }
    }

    class CallDeviceTypesApiInBackground extends android.os.AsyncTask<DeviceTypesApi, Void, List<DeviceTypeEnvelope>> {

        @Override
        protected List<DeviceTypeEnvelope> doInBackground(DeviceTypesApi... apis) {
            List<DeviceTypeEnvelope> results = new ArrayList<DeviceTypeEnvelope>();
            int i = 0;
            for (String dtid : devicesTypeIds) {
                try
                {
                    results.add(apis[0].getDeviceType(dtid));
                }catch (ApiException e)
                {
                    results.add(null);
                    e.printStackTrace();
                }
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<DeviceTypeEnvelope> results) {
            // Find the corresponding device type names
            final HashMap<String, String> deviceTypeIdToName = new HashMap<String, String>();
            for (int i = 0; i < results.size(); i++) {
                DeviceType deviceType = results.get(i).getData();
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
