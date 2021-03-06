package com.geminifile.core.fileparser.binder;

import com.geminifile.core.service.Service;
import com.geminifile.core.service.localnetworkconn.PeerCommunicatorManager;
import com.geminifile.core.socketmsg.MsgType;
import com.geminifile.core.socketmsg.msgwrapper.MsgWrapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import static com.geminifile.core.CONSTANTS.MYBINDERSFILENAME;
import static com.geminifile.core.CONSTANTS.MYBINDERSPATH;

public class BinderManager {

    private static final List<Binder> binders = new ArrayList<>();
    private static JSONObject myBinders = new JSONObject("{\nbinders: []\n}");
    private static File myBindersFile = new File(MYBINDERSPATH, MYBINDERSFILENAME);

    private static final Vector<BinderFileDelta> binderDeltaOperations = new Vector<>(); // Stores any delta from the id specified in the object

    private static Lock binderLock = new ReentrantLock();

    // MyBinders.conf format
    /*
    {
        binders: [
            {
                name: "",
                id:  "",
                path: "",
                lastModified: xx // If lastModified mismatches the current lastModified, then communicate with server.
            }, ...
        ]
    }
     */

    public static void start() {
        Service.LOGGER.info("[FILE] Binder Manager is Starting...");
        binderDeltaOperations.clear(); // Clears delta operations

        for (Binder e : binders) { // Clears the watcher
            e.stopWatcher();
        }

        binders.clear(); // clear binders

        // Checks for a configuration file. If there is no configuration, then create it.
        if (!myBindersFile.exists() && !myBindersFile.isDirectory()) {
            // Creates the file
            try {
                //noinspection ResultOfMethodCallIgnored
                myBindersFile.createNewFile();
                // Creates a default entry in the file
                saveMyBinders();
            } catch (IOException e) {
                Service.LOGGER.severe("[FILE] Failed to create MyBinders file at: " + myBindersFile.getAbsolutePath());
                Service.LOGGER.log(Level.SEVERE, "exception", e);
                System.exit(5);
            }
        }

        // Loads the binder configuration
        loadMyBinders();

        // Start threads to detect change in directory.
        for (Binder e : binders) {
            e.startWatcher();
        }
    }

    public static List<Binder> getAllBinders() {
        return binders;
    }

    public static Vector<BinderFileDelta> getAllBinderFileDelta() {
        return binderDeltaOperations;
    }

    public static void updateMyBinders() {
        // Updates the JSONObject myBinders with the current arraylist
        // Iterate within the binders ArrayList
        binderLock.lock();
        try {
            JSONObject newBinders = new JSONObject();
            JSONArray binderArr = new JSONArray();
            for (Binder e : binders) {
                JSONObject binderJSON = new JSONObject();
                binderJSON.put("name", e.getName());
                binderJSON.put("id", e.getId());
                binderJSON.put("path", e.getDirectory().getAbsolutePath());
                binderJSON.put("lastModified", e.getDirectoryLastModified());
                binderArr.put(binderJSON);
            }
            newBinders.put("binders", binderArr);
            myBinders = newBinders;
        } finally {
            binderLock.unlock();
        }
    }

    public static void saveMyBinders() {
        Service.LOGGER.info("Saved binders to JSON file at :" + myBindersFile.getAbsolutePath());
        // First, update all of the myBinders JSONObject, and saves it.
        updateMyBinders();
        // Writes into file
        try {
            FileWriter writer = new FileWriter(myBindersFile);
            myBinders.write(writer, 4, 0);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Service.LOGGER.severe("[FILE] Failed to open MyBinders file at: " + myBindersFile.getAbsolutePath());
            Service.LOGGER.log(Level.SEVERE, "exception", e);
        }
    }

    public static void loadMyBinders() {
        Service.LOGGER.info("Loaded binders JSON from:" + myBindersFile.getAbsolutePath());
        // Read file and put in myBinders
        try {
            Scanner fileReader = new Scanner(myBindersFile);
            StringBuilder content = new StringBuilder();
            while (fileReader.hasNext()) {
                content.append(fileReader.nextLine());
            }
            myBinders = new JSONObject(content.toString());
        } catch (JSONException e) {
            Service.LOGGER.severe("[FILE] Error opening " + MYBINDERSPATH + MYBINDERSFILENAME);
            Service.LOGGER.log(Level.SEVERE, "Exception", e);
            System.exit(5);
        } catch (FileNotFoundException e) {
            Service.LOGGER.severe("[FILE] MyBinders file is not found: " + myBindersFile.getAbsolutePath());
            Service.LOGGER.log(Level.SEVERE, "exception", e);
        }
        // Loads json file to binder array
        JSONArray jArr = myBinders.getJSONArray("binders");
        // Iterates within the array
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject binderJSON = jArr.getJSONObject(i);
            Binder newBinder = new Binder(
                    binderJSON.getString("name"),
                    binderJSON.getString("id"),
                    new File(binderJSON.getString("path"))
            );
            newBinder.setDirectoryLastModified(binderJSON.getLong("lastModified"));
            binders.add(newBinder);
        }
    }

    public static Binder getBinder(String id) {
        for (Binder e : binders) {
            if (e.getId().equals(id)) return e;
        }
        return null;
    }

    public static MsgWrapper getAskBinderHave() {
        // Will return a message with content: "AskBinderHave-id1-id2-id3-id4"
        StringBuilder content = new StringBuilder("AskBinderHave-");
        for (Binder e : binders) {
            content.append(e.getId()).append("-");
        }
        return new MsgWrapper(content.toString(), MsgType.ASK);
    }

    public static void addBinderDeltaOperation(BinderFileDelta delta) {
        binderDeltaOperations.add(delta);
    }

    public static void removeBinderDeltaOperation(BinderFileDelta delta) {
        if (delta.closeBinderFileDelta()) {
            binderDeltaOperations.remove(delta);
        }
    }

    public static boolean isTokenInBinderDeltas(String token) {
        for (BinderFileDelta e : binderDeltaOperations) {
            if (e.getToken().equals(token)) return true;
        }
        return false;
    }

    public static BinderFileDelta getBinderFileDelta(String token) {
        for (BinderFileDelta e : binderDeltaOperations) {
            if (e.getToken().equals(token)) return e;
        }
        return null;
    }

    public static void restartService() {
        Service.LOGGER.info("[FILE] Binder Manager is Restarting...");
        for (Binder e : binders) {
            e.stopWatcher();
        }
        BinderManager.start();
    }

    public static void clearBinderList() {
        binders.clear();
    }

    public static void addNewBinder(Binder binder) {
        binders.add(binder); // Adds into list
        binder.startWatcher(); // Starts the watcher
        Service.LOGGER.info("Added new binder to the device !" + binder.toString());
        BinderManager.saveMyBinders(); // Saves into json
        PeerCommunicatorManager.sendToAllPeers(BinderManager.getAskBinderHave());
    }

    public static void removeBinder(Binder binder) {
        binder.stopWatcher(); // Stops the watcher
        binders.remove(binder); // Removes from the list
        Service.LOGGER.info("Removed binder from device !" + binder.toString());
        BinderManager.saveMyBinders(); // Saves the json
    }
}