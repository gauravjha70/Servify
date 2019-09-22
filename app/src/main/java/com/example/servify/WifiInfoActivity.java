package com.example.servify;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
public class WifiInfoActivity extends AppCompatActivity{
    private Button btnScan;
    ArrayList<WifiData> datalist;
    ArrayList<String> ipList;
    String subnetIP;
    int defaultGateway;
    String mask;
    String subnet;
    WifiData data;

    SecurityManager securityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_info);
        btnScan = findViewById(R.id.search);
        WifiManager wifiManager;
        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        securityManager = new SecurityManager();
        securityManager = System.getSecurityManager();

        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        defaultGateway = dhcpInfo.gateway;
        int subnetMask = dhcpInfo.netmask;
        subnetIP = FormatIP(defaultGateway);
        System.out.println("Subnet ID : " + subnetIP);

        String subnetIds[] = subnetIP.split("\\.");
        mask = FormatIP(subnetMask);

        subnet = subnetIds[0] + "." + subnetIds[1] + "." + subnetIds[2] + ".";

        System.out.println("New subnet : "+subnet);

        ipList = new ArrayList();
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ScanIpTask().execute();
            }
        });
    }
    private class ScanIpTask extends AsyncTask<Void, String, Void> {


        static final int lower = 150;
        static final int upper = 255;
        static final int timeout = 500;


        @Override
        protected void onPreExecute() {
            ipList.clear();
            datalist = new ArrayList<WifiData>();
            Toast.makeText(getApplicationContext(), "Scan IP...", Toast.LENGTH_LONG).show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            for (int i = lower; i <= upper; i++) {
                String host = subnet + i;
                try {
                    InetAddress inetAddress = InetAddress.getByName(host);
                    if (inetAddress.isReachable(1000)){
                        System.out.println("InetAddress : " + inetAddress.toString());
                        System.out.println("HostName : " + inetAddress.getCanonicalHostName());
                        publishProgress(inetAddress.toString());
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            ipList.add(values[0]);
            data=new WifiData();
            String[] ipList = values[0].split("/");
            String ip = ipList[1];
            data.IpAddress=values[0];

            String macAdd = getMacFromArpCache(values[0]);
            /*if(macAdd!=null)
            {*/
                data.MacAddress=getMacFromArpCache(values[0]);
                System.out.println("IpAddress : " + ip);
                System.out.println("MacAddress : " + getMacFromArpCache(values[0]));
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url ="https://api.macvendors.com/"+getMacFromArpCache(values[0]);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                data.Vendor=response;
                                System.out.println(response);
                                try{
                                    /*securityManager = System.getSecurityManager();
                                    securityManager.checkConnect(ip,-1);*/
                                    InetAddress addr = InetAddress.getByName("172.16.213.168");
//                                    System.out.println("Inet Address : " + addr);
                                    boolean reachable = addr.isReachable(2000);
                                    String host;
                                    if(reachable){
                                        host = addr.getCanonicalHostName();
                                    }
                                    else
                                    {
                                        host = "*****";
                                    }

                                    data.Host=host;
                                    System.out.println("Host : " + host);
                                }
                                catch (Exception e){
                                    System.out.println("Inet Error : " + e.getMessage());
                                    data.Host="****";
                                }
                                datalist.add(data);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
                    }
                });
                queue.add(stringRequest);
                Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_LONG).show();
/*
            }*/

        }
        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_LONG).show();
        }
    }
    public static String getMacFromArpCache(String ip) {
        if (ip == null)
            return null;
        String[] ipList = ip.split("/");
        ip = ipList[1];
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
                    // Basic sanity check
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        return mac;
                    } else {
                        System.out.println("Mac Matches Error");
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String FormatIP(int IpAddress)
    {
        return Formatter.formatIpAddress(IpAddress);
    }
}
