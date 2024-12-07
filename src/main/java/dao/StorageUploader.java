package dao;


import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;

public class StorageUploader {

    private BlobContainerClient containerClient;

    public StorageUploader( ) {
        this.containerClient = new BlobContainerClientBuilder()
                .connectionString("DefaultEndpointsProtocol=https;AccountName=simonstorge;AccountKey=91ICOrMaQ7trd19N0T7TZtzltea8XLbqWStugUXUaZOWweLbQ5lelLgL5/SUiMteBGR3IdaaH5P9+AStym7BOQ==;EndpointSuffix=core.windows.net")
                .containerName("media-files")
                .buildClient();
    }

    public void uploadFile(String filePath, String blobName) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            // Check if blob already exists
            if (blobClient.exists()) {
                // Generate a unique name if the blob exists
                String uniqueBlobName = "profiles/" + System.currentTimeMillis() + "_" + blobName;
                blobClient = containerClient.getBlobClient(uniqueBlobName);
            }

            // Upload the file
            blobClient.uploadFromFile(filePath);
            System.out.println("File uploaded to: " + blobClient.getBlobUrl());
        } catch (BlobStorageException e) {
            System.err.println("Error uploading file: " + e.getMessage());
            throw e;
        }
    }

    public BlobContainerClient getContainerClient(){
        return containerClient;
    }
}
