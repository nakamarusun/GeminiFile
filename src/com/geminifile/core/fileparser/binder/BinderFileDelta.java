package com.geminifile.core.fileparser.binder;

import com.geminifile.core.MathUtil;
import com.geminifile.core.service.Service;
import com.geminifile.core.service.localnetworkconn.PeerCommunicationLoop;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

public class BinderFileDelta implements Serializable {

    private final String token; // Special random alphanumeric characters that represent this delta operation.

    private String peerNodeId; // Other node's id that this delta operation is handling

    private final String id; // Binder id that this file delta is handling.
    private final Vector<String> thisPeerNeed = new Vector<>();
    private final Vector<String> otherPeerNeed = new Vector<>();

    private Status status; // Operation status

    private int closeCounter; // Tool to safely remove this BinderFileDelta from BinderManager safely requiring 2 removal process.

    public BinderFileDelta(String id) {
        // Empty delta operation
        token = MathUtil.generateRandomAlphaNum(10);
        this.peerNodeId = "";
        this.id = id;
        this.status = Status.IDLE;
        closeCounter = 0;

    }

    public BinderFileDelta(String id, String token) {
        // Empty delta operation
        this.token = token;
        this.peerNodeId = "";
        this.id = id;
        this.status = Status.IDLE;
        closeCounter = 0;
    }

    public BinderFileDelta(String id, PeerCommunicationLoop peerToSend, ArrayList<FileListing> thisPeerListing, ArrayList<FileListing> otherPeerListing) {
        // This function is used to calculate what machine needs what file.
        if (peerToSend == null) {
            this.peerNodeId = "";
        } else {
            this.peerNodeId = peerToSend.getNode().getId();
        }
        this.id = id;
        this.status = Status.IDLE;
        token = MathUtil.generateRandomAlphaNum(10);
        closeCounter = 0;

        @SuppressWarnings("unchecked")
        ArrayList<FileListing> otherPeerListingCopy = (ArrayList<FileListing>) otherPeerListing.clone(); // A copy of the otherPeerListing to know the difference.

        // This loops all the file entry in both listings, and puts them in the delta.
        for (FileListing ei : thisPeerListing) {

            boolean fileIsFound = false;
            String fileI = ei.getRelativePath().replace("\\", "/"); // Make the string of both entries to change their separators.

            for (FileListing ej : otherPeerListing) {
                // Iterates within all of the files.
                String fileJ = ej.getRelativePath().replace( "\\", "/"); // Make the string of both entries to change their separators.
                if (fileI.equals(fileJ)) {

                    if (!ei.getCheckSumMD5().equals(ej.getCheckSumMD5())) { // If both files has a different checksum
                        // Evaluates delta by time
                        // If the filenames are the same, check the lastModified times.
                        if (ei.getLastModified() > ej.getLastModified()) {
                            // The file in the current device is newer than the other device
                            otherPeerNeed.add(fileI);
                        } else if (ei.getLastModified() < ej.getLastModified()) {
                            // The file in the other device is newer than the current device
                            thisPeerNeed.add(fileJ);
                        }
                    }

                    fileIsFound = true;
                    otherPeerListingCopy.remove(ej);
                    otherPeerListing.remove(ej); // To make things more efficient, remove from list once it is found to be same file.
                    // If a file with the same name is in the device, then break the searching and continue with the other.
                    break;
                }
            }
            if (!fileIsFound) {
                // if the file in thisPeerListing is not found in otherPeerListing then
                otherPeerNeed.add(fileI);
            }
        }
        // Adds from the other copy to the need list.
        for (FileListing e : otherPeerListingCopy) {
            thisPeerNeed.add(e.getRelativePath().replace("\\", "/")); // Replaces with linux / unix character
        }

    }

    public BinderFileDelta(JSONObject binderDeltaJSON) {

        token = binderDeltaJSON.getString("token"); // Sets token
        id = binderDeltaJSON.getString("id"); // Sets id
        status = Status.IDLE;
        closeCounter = 0;

        peerNodeId = binderDeltaJSON.getString("peerNodeId");

        for (int i = 0; i < binderDeltaJSON.getJSONArray("thisPeerNeed").length(); i++) {
            thisPeerNeed.add(binderDeltaJSON.getJSONArray("thisPeerNeed").getString(i)); // Sets thisPeerNeed
        }

        for (int i = 0; i < binderDeltaJSON.getJSONArray("otherPeerNeed").length(); i++) {
            otherPeerNeed.add(binderDeltaJSON.getJSONArray("otherPeerNeed").getString(i)); // setsOtherPeerNeed
        }

    }

    public enum Status {
        IDLE,
        INPROCESS,
        FAILED
    }


    public String getToken() {
        return token;
    }

    public String getId() {
        return id;
    }

    public String getPeerNodeId() {
        return peerNodeId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setPeerNodeId(String peerNodeId) {
        this.peerNodeId = peerNodeId;
    }

    public void addOtherPeerNeed(String otherPeerNeed) {
        this.otherPeerNeed.add(otherPeerNeed);
    }

    public void addThisPeedNeed(String thisPeerNeed) {
        this.thisPeerNeed.add(thisPeerNeed);
    }

    public JSONObject getBinderDeltaJSON() {
        JSONObject json = new JSONObject();

        json.put("token", token); // Puts the token
        json.put("id", id); // Puts the id
        json.put("peerNodeId", peerNodeId);

        // Puts the thisPeerNeed vector
        JSONArray thisPeerNeedJSON = new JSONArray();
        for (String e : thisPeerNeed) {
            thisPeerNeedJSON.put(e);
        }
        json.put("thisPeerNeed", thisPeerNeed);

        // Puts the otherPeerNeed vector
        JSONArray otherPeerNeedJSON = new JSONArray();
        for (String e : otherPeerNeed) {
            otherPeerNeedJSON.put(e);
        }
        json.put("otherPeerNeed", otherPeerNeed);

        return json;
    }

    public JSONObject getSwitchedBinderDeltaJSON() {
        JSONObject json = getBinderDeltaJSON();

        // Switches the thisPeerNeed and otherPeerNeed
        JSONArray temp = json.getJSONArray("thisPeerNeed");

        json.put("thisPeerNeed", json.getJSONArray("otherPeerNeed"));
        json.put("otherPeerNeed", temp);

        return json;
    }

    public Vector<String> getThisPeerNeed() {
        return thisPeerNeed;
    }

    public Vector<String> getOtherPeerNeed() {
        return otherPeerNeed;
    }

    public void printAllDelta() {
        Service.LOGGER.info("This device needs: " + thisPeerNeed.toString());
        Service.LOGGER.info("Other device needs: " + otherPeerNeed.toString());
    }

    public boolean closeBinderFileDelta() {
        // If this returns true, then you can safely remove this binderFileDelta from binderDeltaOperations @ BinderManager class
        if (closeCounter == 1) {
            return true;
        }
        closeCounter++;
        return false;
    }

}