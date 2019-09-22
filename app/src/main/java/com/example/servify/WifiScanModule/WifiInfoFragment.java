package com.example.servify.WifiScanModule;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.servify.ObjectModel;
import com.example.servify.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class WifiInfoFragment extends Fragment {
    private Button btnScan;
    ArrayList<WifiData> datalist;
    ArrayList<String> ipList;

    ArrayList<ObjectModel> objectModels;

    String subnetIP;
    int defaultGateway;
    String mask;
    String subnet;
    WifiData data;

    SecurityManager securityManager;

    RecyclerView deviceList;
    ScanRecyclerAdapter adapter;

    CardView loader;
    TextView doneText;
    ProgressBar progressBar;

    FloatingActionButton addButton;

    List<ObjectModel> result;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.wifi_scanner,container,false);

        deviceList = rootView.findViewById(R.id.recycler_view);
        loader = rootView.findViewById(R.id.loader_card);
        addButton = rootView.findViewById(R.id.add_button);
        progressBar = rootView.findViewById(R.id.progress_bar);
        doneText = rootView.findViewById(R.id.done);

        objectModels = new ArrayList<ObjectModel>();
        adapter = new ScanRecyclerAdapter(getContext(), objectModels);
        deviceList.setLayoutManager(new LinearLayoutManager(getActivity()));

        deviceList.setAdapter(adapter);

        startScan();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result = adapter.getSelected();
            }
        });

        return rootView;
    }

    void startScan()
    {
        WifiManager wifiManager;
        wifiManager = (WifiManager)getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

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

        new ScanIpTask().execute();
    }


    private class ScanIpTask extends AsyncTask<Void, String, Void> {


        static final int lower = 150;
        static final int upper = 255;
        static final int timeout = 100;

        String host;


        @Override
        protected void onPreExecute() {
            objectModels = new ArrayList<ObjectModel>();
            datalist = new ArrayList<WifiData>();
            doneText.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            loader.setVisibility(View.VISIBLE);

        }
        @Override
        protected Void doInBackground(Void... params) {

            objectModels = new ArrayList<ObjectModel>();

            for (int i = lower; i <= upper; i++) {
                String host = subnet + i;
                try {
                    InetAddress inetAddress = InetAddress.getByName(host);
                    if (inetAddress.isReachable(timeout)){
                        publishProgress(inetAddress.toString(), inetAddress.getHostName().toString());
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

            String[] ipList = values[0].split("/");
            String ip = ipList[1];

            host = values[1];

            ObjectModel obj = new ObjectModel();
            String macAdd = getMacFromArpCache(values[0]);

            if(macAdd!=null)
            {
                System.out.println("IpAddress : " + ip);
                System.out.println("MacAddress : " + getMacFromArpCache(values[0]));
                RequestQueue queue = Volley.newRequestQueue(getContext());

                if(host.equals(ip))
                {
                    host = "Generic";
                }

                System.out.println("Host : " + host);

                String url ="https://api.macvendors.com/"+getMacFromArpCache(values[0]);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                System.out.println(response);
                                obj.setIpAddress(ip);
                                obj.setHost(host);
                                obj.setMacAddress(macAdd);
                                obj.setVendor(response);

                                objectModels.add(obj);

                                System.out.println("Size : " + objectModels.size());

                                adapter = new ScanRecyclerAdapter(getContext(),objectModels);
                                deviceList.setAdapter(adapter);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        obj.setIpAddress(ip);
                        obj.setHost(host);
                        obj.setMacAddress(macAdd);
                        obj.setVendor("Generic");

                        objectModels.add(obj);
                        adapter = new ScanRecyclerAdapter(getContext(),objectModels);
                        deviceList.setAdapter(adapter);
                    }
                });
                queue.add(stringRequest);
                Toast.makeText(getContext(), values[0], Toast.LENGTH_LONG).show();
            }

        }
        @Override
        protected void onPostExecute(Void aVoid) {
            doneText.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loader.setVisibility(View.GONE);
                }
            }, 2000);


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
