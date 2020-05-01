package openfoodfacts.github.scrachx.openfood.jobs;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;

/**
 * Class which is used to upload the saved product by extending Job service
 *
 * @author jayanth
 * @since 22/2/2018
 */
public class SavedProductUploadJob extends JobService {
    OpenFoodAPIClient apiClient;

    /**
     * A method to start uploading the saved product.
     *
     * @param job: job parameters
     * @return boolean state of the Job
     */
    @Override
    public boolean onStartJob(JobParameters job) {
        apiClient = new OpenFoodAPIClient(this);
        apiClient.uploadOfflineImages(this, false, job, this);
        return true;
    }

    /**
     * A method to stop uploading the saved product.
     *
     * @param job: job parameters
     * @return boolean state of the Job
     */
    @Override
    public boolean onStopJob(JobParameters job) {
        apiClient.uploadOfflineImages(this, true, job, this);
        return true;
    }
}
