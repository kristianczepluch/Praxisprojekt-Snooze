package com.kristian.czepluch.snoozification.HelperClasses;

import android.app.job.JobParameters;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.kristian.czepluch.snoozification.Interfaces.AsyncUser;
import com.kristian.czepluch.snoozification.Datastructures.Anwendung;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class CategoryAsyncTask extends AsyncTask<ArrayList<String>, Integer, ArrayList<String>> {

    private static final String TAG = "CategoryAsyncTask";
    private ArrayList<String> allpkgs;
    private ArrayList<Anwendung> anwendungArrayList = new ArrayList<>();
    private String mode;
    private AsyncUser asyncUser;
    private Context context;
    private JobParameters mParams;

    public void setMode(String mode) {
        this.mode = mode;
    }

    public CategoryAsyncTask(AsyncUser asyncUser, Context context, String mode) {
        this.asyncUser = asyncUser;
        this.context = context;
        this.mode = mode;
    }

    public CategoryAsyncTask(AsyncUser asyncUser, Context context, String mode, JobParameters mParams) {
        this.asyncUser = asyncUser;
        this.context = context;
        this.mode = mode;
        this.mParams = mParams;
    }

    @Override
    protected ArrayList<String> doInBackground(ArrayList<String>... arrayLists) {
        allpkgs = arrayLists[0];
        ArrayList<String> allcats = new ArrayList<>();
        for (int i = 0; i < arrayLists[0].size(); i++) {
            String appCategoryType = parseAndExtractCategory(arrayLists[0].get(i));
            allcats.add(appCategoryType);
        }
        return allcats;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ArrayList<String> s) {
        super.onPostExecute(s);

        if (mode.equals("INIT")) {
            for (int i = 0; i < s.size(); i++) {
                anwendungArrayList.add(new Anwendung(allpkgs.get(i), s.get(i)));
            }

            // After sucessfully analyzing each BeaconConsumerApplication for their category, a list will be stored on the local storage by their category name
            ArrayList<String> GAMES = new ArrayList<>();
            ArrayList<String> SOCIAL = new ArrayList<>();
            ArrayList<String> COMMUNICATION = new ArrayList<>();
            ArrayList<String> NEWS_AND_MAGAZINES = new ArrayList<>();
            ArrayList<String> PRODUCTIVITY_AND_EDUCATION = new ArrayList<>();
            ;
            ArrayList<String> BUSINESS_AND_FINANCES = new ArrayList<>();
            ArrayList<String> HEALTH_AND_FITNESS = new ArrayList<>();
            ArrayList<String> MEDICAL = new ArrayList<>();
            ArrayList<String> FOOD_AND_DRINKS = new ArrayList<>();
            ArrayList<String> LIFESTYLE = new ArrayList<>();
            ArrayList<String> OTHERS = new ArrayList<>();

            for (Anwendung anwendung : anwendungArrayList) {
                String category = anwendung.getCategory();
                if (category.equals("GAMES")) {
                    GAMES.add(anwendung.getPackageName());
                } else if (category.equals("SOCIAL")) {
                    SOCIAL.add(anwendung.getPackageName());
                } else if (category.equals("COMMUNICATION")) {
                    COMMUNICATION.add(anwendung.getPackageName());
                } else if (category.equals("NEWS & MAGAZINES")) {
                    NEWS_AND_MAGAZINES.add(anwendung.getPackageName());
                } else if (category.equals("PRODUCTIVITY") || category.equals("EDUCATION")) {
                    PRODUCTIVITY_AND_EDUCATION.add(anwendung.getPackageName());
                } else if (category.equals("BUSINESS") || category.equals("FINANCES")) {
                    BUSINESS_AND_FINANCES.add(anwendung.getPackageName());
                } else if (category.equals("HEALTH & FITNESS") || category.equals("SPORTS")) {
                    HEALTH_AND_FITNESS.add(anwendung.getPackageName());
                } else if (category.equals("FOOD & DRINKS")) {
                    FOOD_AND_DRINKS.add(anwendung.getPackageName());
                } else if (category.equals("LIFESTYLE")) {
                    LIFESTYLE.add(anwendung.getPackageName());
                } else {
                    OTHERS.add(anwendung.getPackageName());
                }
            }

            // After all Applications are saved in Lists, each List will be saved in Shared Preferences
            TinyDB tinydb = new TinyDB(context);
            tinydb.putListString("GAMES", GAMES);
            tinydb.putListString("SOCIAL", SOCIAL);
            tinydb.putListString("COMMUNICATION", COMMUNICATION);
            tinydb.putListString("NEWS_AND_MAGAZINES", NEWS_AND_MAGAZINES);
            tinydb.putListString("PRODUCTIVITY", PRODUCTIVITY_AND_EDUCATION);
            tinydb.putListString("BUSINESS", BUSINESS_AND_FINANCES);
            tinydb.putListString("HEALTH_AND_FITNESS", HEALTH_AND_FITNESS);
            tinydb.putListString("MEDICAL", MEDICAL);
            tinydb.putListString("FOOD_AND_DRINKS", FOOD_AND_DRINKS);
            tinydb.putListString("LIFESTYLE", LIFESTYLE);
            tinydb.putListString("OTHERS", OTHERS);

        } else if (mode.equals("ADD")) {
            for (int i = 0; i < s.size(); i++) {
                anwendungArrayList.add(new Anwendung(allpkgs.get(i), s.get(i)));
                Log.e(TAG, "Added: " + s.get(i));
            }
            ArrayList<String> GAMES = new ArrayList<>();
            ArrayList<String> SOCIAL = new ArrayList<>();
            ArrayList<String> COMMUNICATION = new ArrayList<>();
            ArrayList<String> NEWS_AND_MAGAZINES = new ArrayList<>();
            ArrayList<String> PRODUCTIVITY_AND_EDUCATION = new ArrayList<>();
            ;
            ArrayList<String> BUSINESS_AND_FINANCES = new ArrayList<>();
            ArrayList<String> HEALTH_AND_FITNESS = new ArrayList<>();
            ArrayList<String> MEDICAL = new ArrayList<>();
            ArrayList<String> FOOD_AND_DRINKS = new ArrayList<>();
            ArrayList<String> LIFESTYLE = new ArrayList<>();
            ArrayList<String> OTHERS = new ArrayList<>();

            TinyDB tinydb = new TinyDB(context);
            for (Anwendung anwendung : anwendungArrayList) {
                String category = anwendung.getCategory();
                Log.e(TAG, "Anwendung: " + category + "added");
                if (category.equals("GAMES")) {
                    GAMES.add(anwendung.getPackageName());
                } else if (category.equals("SOCIAL")) {
                    SOCIAL.add(anwendung.getPackageName());
                } else if (category.equals("COMMUNICATION")) {
                    COMMUNICATION.add(anwendung.getPackageName());
                } else if (category.equals("NEWS & MAGAZINES")) {
                    NEWS_AND_MAGAZINES.add(anwendung.getPackageName());
                } else if (category.equals("PRODUCTIVITY") || category.equals("EDUCATION")) {
                    PRODUCTIVITY_AND_EDUCATION.add(anwendung.getPackageName());
                } else if (category.equals("BUSINESS") || category.equals("FINANCES")) {
                    BUSINESS_AND_FINANCES.add(anwendung.getPackageName());
                } else if (category.equals("HEALTH & FITNESS") || category.equals("SPORTS")) {
                    HEALTH_AND_FITNESS.add(anwendung.getPackageName());
                } else if (category.equals("MEDICAL")) {
                    MEDICAL.add(anwendung.getPackageName());
                } else if (category.equals("FOOD & DRINKS")) {
                    FOOD_AND_DRINKS.add(anwendung.getPackageName());
                } else if (category.equals("LIFESTYLE")) {
                    LIFESTYLE.add(anwendung.getPackageName());
                } else {
                    OTHERS.add(anwendung.getPackageName());
                }
            }

            // After all Applications are saved in Lists, each List will be saved in Shared Preferences
            ArrayList<String> oldList = tinydb.getListString("GAMES");
            oldList.addAll(GAMES);
            tinydb.putListString("GAMES", oldList);

            oldList = tinydb.getListString("SOCIAL");
            oldList.addAll(SOCIAL);
            tinydb.putListString("SOCIAL", oldList);


            oldList = tinydb.getListString("COMMUNICATION");
            oldList.addAll(COMMUNICATION);
            tinydb.putListString("COMMUNICATION", oldList);


            oldList = tinydb.getListString("NEWS_AND_MAGAZINES");
            oldList.addAll(NEWS_AND_MAGAZINES);
            tinydb.putListString("NEWS_AND_MAGAZINES", oldList);


            oldList = tinydb.getListString("PRODUCTIVITY");
            oldList.addAll(PRODUCTIVITY_AND_EDUCATION);
            tinydb.putListString("PRODUCTIVITY", oldList);


            oldList = tinydb.getListString("BUSINESS");
            oldList.addAll(BUSINESS_AND_FINANCES);
            tinydb.putListString("BUSINESS", oldList);


            oldList = tinydb.getListString("HEALTH_AND_FITNESS");
            oldList.addAll(HEALTH_AND_FITNESS);
            tinydb.putListString("HEALTH_AND_FITNESS", oldList);


            oldList = tinydb.getListString("MEDICAL");
            oldList.addAll(GAMES);
            tinydb.putListString("MEDICAL", oldList);

            oldList = tinydb.getListString("FOOD_AND_DRINKS");
            oldList.addAll(FOOD_AND_DRINKS);
            tinydb.putListString("FOOD_AND_DRINKS", oldList);


            oldList = tinydb.getListString("LIFESTYLE");
            oldList.addAll(LIFESTYLE);
            tinydb.putListString("LIFESTYLE", oldList);


            oldList = tinydb.getListString("OTHERS");
            oldList.addAll(OTHERS);
            tinydb.putListString("OTHERS", oldList);


        }
        anwendungArrayList.clear();
        if(mParams == null) asyncUser.onFinish();
        else asyncUser.onFinishJob(mParams);

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }


    private String parseAndExtractCategory(String packageName) {

        String APP_URL = "https://play.google.com/store/apps/details?id=";
        String url = APP_URL + packageName + "&hl=en";
        String appCategoryType = null;
        String appName = null;
        String CATEGORY_STRING = "category/";

        String extractionApps = "com.android.providers.downloads.ui, com.android.contacts," +
                " com.android.gallery3d, com.android.vending, com.android.calculator2, com.android.calculator," +
                " com.android.deskclock, com.android.messaging, com.android.settings, com.android.stk";

        try {

            if (!extractionApps.contains(packageName)) {
                Document doc = null;
                try {
                    doc = Jsoup.connect(url).get();

                    if (doc != null) {
                        if (appCategoryType == null || appCategoryType.length() < 1) {
                            Elements text = doc.select("a[itemprop=genre]");

                            if (text != null) {
                                if (appCategoryType == null || appCategoryType.length() < 2) {
                                    String href = text.attr("abs:href");
                                    if (href != null && href.length() > 4 && href.contains(CATEGORY_STRING)) {
                                        appCategoryType = getCategoryTypeByHref(href);
                                    }
                                }
                            }
                        }
                        //TODO: END_METHOD_2

                        if (appCategoryType != null && appCategoryType.length() > 1) {
                            appCategoryType = replaceSpecialCharacter(appCategoryType);
                        }

                    }
                } catch (IOException e) {
                    appCategoryType = "OTHERS";
                }
            } else {
                appCategoryType = "OTHERS";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appCategoryType;
    }

    private String getCategoryTypeByHref(String href) {
        int cat_size = 9;
        String CATEGORY_STRING = "category/";
        String appCategoryType = null;
        String CATEGORY_GAME_STRING = "GAME_";
        try {
            appCategoryType = href.substring((href.indexOf(CATEGORY_STRING) + cat_size), href.length());
            if (appCategoryType != null && appCategoryType.length() > 1) {
                if (appCategoryType.contains(CATEGORY_GAME_STRING)) {
                    //appCategoryType = appContext.getString(R.string.games);
                    appCategoryType = "GAMES";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appCategoryType;
    }

    private String replaceSpecialCharacter(String appCategoryType) {
        try {
            //Find and Replace '&amp;' with '&' in category Text
            if (appCategoryType.contains("&amp;")) {
                appCategoryType = appCategoryType.replace("&amp;", " & ");
            }

            //Find and Replace '_AND_' with ' & ' in category Text
            if (appCategoryType.contains("_AND_")) {
                appCategoryType = appCategoryType.replace("_AND_", " & ");
            }

            //Find and Replace '_' with ' ' <space> in category Text
            if (appCategoryType.contains("_")) {
                appCategoryType = appCategoryType.replace("_", " ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appCategoryType;
    }
}
