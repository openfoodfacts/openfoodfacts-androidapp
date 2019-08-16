package openfoodfacts.github.scrachx.openfood.jobs;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;

/**
 * Created by jayanth on 22/2/18.
 */

public class SavedProductUploadJob extends JobService {
    OpenFoodAPIClient apiClient;

    @Override
    public boolean onStartJob(JobParameters job) {
        apiClient = new OpenFoodAPIClient(this);
        apiClient.uploadOfflineImages(this, false, job, this);
        return true;
    }


    @Override
    public boolean onStopJob(JobParameters job) {
        apiClient.uploadOfflineImages(this, true, job, this);
        return true;
    }

}
