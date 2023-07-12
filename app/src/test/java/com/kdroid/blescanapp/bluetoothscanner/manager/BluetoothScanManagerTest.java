package com.kdroid.blescanapp.bluetoothscanner.manager;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanSettings;

import com.kdroid.blescanapp.bluetoothscanner.callbacks.BluetoothScannerCallback;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class BluetoothScanManagerTest {

    @Mock
    BluetoothManager mockBtManager;
    @Mock
    BluetoothAdapter mockBtAdapter;
    @Mock
    BluetoothLeScanner mockBleScanner;
    @Mock
    BluetoothScannerCallback mockScanCallback;

    private BluetoothScanManager bluetoothScanManager;

    @Before
    public void setup() {
        when(mockBtManager.getAdapter()).thenReturn(mockBtAdapter);
        when(mockBtAdapter.getBluetoothLeScanner()).thenReturn(mockBleScanner);
        bluetoothScanManager = new BluetoothScanManager(mockBtManager, BluetoothScanManager.DEFAULT_SCAN_PERIOD, mock(ScanSettings.class), new ArrayList<>(), mockScanCallback);
    }

    @Test
    public void scanBleDevices_StartsAndStopsScan() {
        bluetoothScanManager.scanBleDevices();

        verify(mockBleScanner).startScan(anyList(), any(ScanSettings.class), eq(mockScanCallback));

        verify(mockBleScanner, timeout(BluetoothScanManager.DEFAULT_SCAN_PERIOD)).stopScan(eq(mockScanCallback));
    }

    @Test
    public void scanBleDevices_StopsScanIfAlreadyScanning() {
        // Set scanning to true
        setField(bluetoothScanManager, "scanning", true);

        bluetoothScanManager.scanBleDevices();

        verify(mockBleScanner).stopScan(eq(mockScanCallback));
    }

    @Test
    public void executeBeforeScanActions_CallsAllActions() {
        List<Runnable> mockActions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Runnable mockAction = mock(Runnable.class);
            mockActions.add(mockAction);
            bluetoothScanManager.getBeforeScanActions().add((Function0<Unit>) mockAction);
        }

        bluetoothScanManager.executeBeforeScanActions();

        for (Runnable mockAction : mockActions) {
            verify(mockAction).run();
        }
    }

    @Test
    public void executeAfterScanActions_CallsAllActions() {
        List<Runnable> mockActions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Runnable mockAction = mock(Runnable.class);
            mockActions.add(mockAction);
            bluetoothScanManager.getAfterScanActions().add((Function0<Unit>) mockAction);
        }

        bluetoothScanManager.executeAfterScanActions();

        for (Runnable mockAction : mockActions) {
            verify(mockAction).run();
        }
    }

    // Helper method to set private field using reflection
    private void setField(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}