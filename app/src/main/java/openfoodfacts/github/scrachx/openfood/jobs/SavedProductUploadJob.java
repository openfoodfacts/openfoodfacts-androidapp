package openfoodfacts.github.scrachx.openfood.jobs;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;

/**
 * Class which is used to upload the saved product by extending Job service
 * @author jayanth
 * @since 22/2/18
 */
public class SavedProductUploadJob extends JobService {
    OpenFoodAPIClient apiClient;

    /**
     * A method to start uploading the saved product.
     * @param job: job parameters
     * @return boolean state of the Job
     */
    @Override
    public boolean onStartJob(JobParameters job) {
        // Toast.makeText(this, "job called", Toast.LENGTH_SHORT).show();
        apiClient = new OpenFoodAPIClient(this);
        apiClient.uploadOfflineImages(this, false, job, this);
        return true;
    }

    /**
     * A method to stop uploading the saved product.
     * @param job: job parameters
     * @return boolean state of the Job
     */
    @Override
    public boolean onStopJob(JobParameters job) {
        // Toast.makeText(this, "job stopped", Toast.LENGTH_SHORT).show();
        apiClient.uploadOfflineImages(this, true, job, this);
        return true;
    }

}
