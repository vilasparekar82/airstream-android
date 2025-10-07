package com.video.airstream.worker;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.video.airstream.apiclient.APIClient;
import com.video.airstream.modal.Device;
import com.video.airstream.modal.Ticker;
import com.video.airstream.service.APIInterface;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import retrofit2.Response;

public class TickerWorker extends Worker {

    public TickerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        String orgId = inputData.getString("orgId");
        String deviceJson = inputData.getString("device_details");
        String host = inputData.getString("HOST");
        Data outputData  = new Data.Builder()
                .putString("device_details", deviceJson)
                .build();;
        if (TextUtils.isEmpty(orgId) && TextUtils.isEmpty(deviceJson)) {
            return Result.failure();
        } else {
            if(TextUtils.isEmpty(orgId) && !TextUtils.isEmpty(deviceJson)){
                Gson gson = new Gson();
                Device device = gson.fromJson(deviceJson, Device.class);
                orgId = device.getDeviceOwner().getOrganization().getOrganizationId().toString();
            }

            APIInterface apiInterface = APIClient.getClient(host).create(APIInterface.class);
            try {
                Response<List<Ticker>> response = apiInterface.getTicker(orgId).execute();
                if(response.isSuccessful()){
                    List<Ticker> tickers = response.body();
                    Optional<String> tickerOptional = null;
                    if (tickers != null) {
                        tickerOptional = tickers.stream().filter(t-> t.getTickerStatus().equalsIgnoreCase("Active")).map(Ticker::getTickerContent)
                                .reduce((t1, t2) -> t1 + "               " +t2);
                    }
                    if(tickerOptional.isPresent()){
                        outputData = new Data.Builder()
                                .putString("device_details", deviceJson)
                                .putString("ticker", tickerOptional.get())
                                .build();
                    }

                }
            } catch (IOException ignored) {
                Result.failure();
            }
        }

        return Result.success(outputData);
    }
}
