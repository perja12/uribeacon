/*
 * Copyright 2015 Google Inc. All rights reserved.
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

package org.uribeacon.validator;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.uribeacon.config.ProtocolV2;

import java.util.ArrayList;
import java.util.List;


public class ScanActivity extends Activity {

  private static final String TAG = ScanActivity.class.getCanonicalName();
  private static final int REQUEST_ENABLE_BT = 1;
  private BluetoothAdapter mBluetoothAdapter;
  private String testType;
  private boolean lockImplemented;
  private ScanCallback mScanCallback = new ScanCallback() {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
      super.onScanResult(callbackType, result);
      Intent intent = new Intent(ScanActivity.this, TestActivity.class);
      intent.putExtra(BluetoothDevice.class.getCanonicalName(), result.getDevice());
      intent.putExtra(MainActivity.LOCK_IMPLEMENTED, lockImplemented);
      intent.putExtra(MainActivity.TEST_TYPE, testType);
      startActivity(intent);
    }
  };

  /////////////////////////////
  //// Lifecycle Callbacks ////
  /////////////////////////////
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    testType = getIntent().getStringExtra(MainActivity.TEST_TYPE);
    lockImplemented = getIntent().getBooleanExtra(MainActivity.LOCK_IMPLEMENTED, false);
    setContentView(R.layout.activity_start);
  }

  @Override
  protected void onResume() {
    super.onResume();

    final BluetoothManager bluetoothManager =
        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    mBluetoothAdapter = bluetoothManager.getAdapter();
    // Check that bluetooth is enabled
    if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    } else {
      startSearchingForBeacons();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    stopSearchingForBeacons();
  }

  private void startSearchingForBeacons() {
    ScanSettings settings = new ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build();
    List<ScanFilter> filters = new ArrayList<>();

    ScanFilter filter = new ScanFilter.Builder()
        .setServiceUuid(ProtocolV2.CONFIG_SERVICE_UUID)
        .build();
    filters.add(filter);
    getLeScanner().startScan(filters, settings, mScanCallback);
    Log.d(TAG, "Started scanning");
  }

  private void stopSearchingForBeacons() {
    getLeScanner().stopScan(mScanCallback);
  }

  private BluetoothLeScanner getLeScanner() {
    return mBluetoothAdapter.getBluetoothLeScanner();
  }

}