package com.example.servify;

import android.content.Context;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class WifiInfoActivityTest extends AppCompatActivity{

    WifiP2pManager wifiManager;
    private WifiP2pManager.Channel channel;

    IntentFilter intentFilter;
    private List<WifiP2pDevice> deviceList;

    Button search;

    //
    public String   s_dns1 ;
    public String   s_dns2;
    public String   s_gateway;
    public String   s_ipAddress;
    public String   s_leaseDuration;
    public String   s_netmask;
    public String   s_serverAddress;
    DhcpInfo d;
    WifiManager wifii;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_info);

        search = findViewById(R.id.search);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchify();
            }
        });

       /* wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiManager.initialize(this, getMainLooper(), null);
        deviceList = new ArrayList<WifiP2pDevice>();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(WifiInfoFragment.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
                        requestPeers();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(WifiInfoFragment.this, "Discovery Failed : " + i, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });*/

    }

    private void searchify()
    {


        wifii = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        d = wifii.getDhcpInfo();

        s_dns1 = "DNS 1: " + String.valueOf(d.dns1);
        s_dns2 = "DNS 2: " + String.valueOf(d.dns2);
        s_gateway = "Default Gateway: " + String.valueOf(d.gateway);
        s_ipAddress = "IP Address: " + String.valueOf(d.ipAddress);
        s_leaseDuration = "Lease Time: " + String.valueOf(d.leaseDuration);
        s_netmask = "Subnet Mask: " + String.valueOf(d.netmask);
        s_serverAddress = "Server IP: " + String.valueOf(d.serverAddress);

        getConnectedIps();
    }

    private void getConnectedIps()
    {
        String connections = "";
        InetAddress host;
        try
        {
            host = InetAddress.getByName(intToIp(d.dns1));
            byte[] ip = host.getAddress();

            for(int i = 1; i <= 254; i++)
            {
                ip[3] = (byte) i;
                InetAddress address = InetAddress.getByAddress(ip);
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }
                if(address.isReachable(100))
                {
                    System.out.println(address + " machine is turned on and can be pinged");
                    connections+= address+"\n";
                }
                else if(!address.getHostAddress().equals(address.getHostName()))
                {
                    System.out.println(address + " machine is known in a DNS lookup");
                }

            }
        }
        catch(UnknownHostException e1)
        {
            e1.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        System.out.println(connections);
    }

    public String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 24) & 0xFF);
    }


  /*  private void requestPeers()
    {
        wifiManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                if (wifiP2pDeviceList.getDeviceList().size() == 0) {
                    System.out.println("No devices found");
                    Toast.makeText(WifiInfoFragment.this, "Not Found", Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    System.out.println("Peers" + wifiP2pDeviceList.getDeviceList());
                    Toast.makeText(WifiInfoFragment.this, "Found : " + wifiP2pDeviceList.getDeviceList().size(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/

}
