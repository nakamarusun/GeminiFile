package com.geminifile.core.fileparser.binder;

import com.geminifile.core.MathUtil;
import com.geminifile.core.service.localnetworkconn.PeerCommunicationLoop;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class BinderFileDelta {

    private final String token;

    private final String peerNodeId;

    private final String id;
    private final Vector<String> thisPeerNeed = new Vector<>();
    private final Vector<String> otherPeerNeed = new Vector<>();

    public BinderFileDelta(String id, PeerCommunicationLoop peerToSend, ArrayList<FileListing> thisPeerListing, ArrayList<FileListing> otherPeerListing) {
        // This function is used to calculate what machine needs what file.
        this.peerNodeId = peerToSend.getNode().getId();
        this.id = id;
        token = MathUtil.generateRandomAlphaNum(7);

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

        peerNodeId = binderDeltaJSON.getString("peerNodeId");

        for (int i = 0; i < binderDeltaJSON.getJSONArray("thisPeerNeed").length(); i++) {
            thisPeerNeed.add(binderDeltaJSON.getJSONArray("thisPeerNeed").getString(i)); // Sets thisPeerNeed
        }

        for (int i = 0; i < binderDeltaJSON.getJSONArray("otherPeerNeed").length(); i++) {
            otherPeerNeed.add(binderDeltaJSON.getJSONArray("otherPeerNeed").getString(i)); // setsOtherPeerNeed
        }

    }

    public String getToken() {
        return token;
    }

    public String getId() {
        return id;
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
        System.out.println("This device needs: " + thisPeerNeed.toString());
        System.out.println("Other device needs: " + otherPeerNeed.toString());
    }

}