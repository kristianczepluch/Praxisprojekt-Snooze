package com.kristian.czepluch.snoozification.HelperClasses;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kristian.czepluch.snoozification.Adapters.RecyclerViewAdapterRulesOverview;
import com.kristian.czepluch.snoozification.Datastructures.Anwendung;
import com.kristian.czepluch.snoozification.Datastructures.Category;
import com.kristian.czepluch.snoozification.Datastructures.Database;
import com.kristian.czepluch.snoozification.Dialogs.Applications_dialogFragment;
import com.kristian.czepluch.snoozification.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentRules extends Fragment implements RecyclerViewAdapterRulesOverview.OnCategoryClicked {

    private View myView;
    private RecyclerView myRecyclerView;
    private List<Category> myCategories;
    private List<String> allCategories;
    private List<Drawable> allIcons;
    private List<String> allCategoriesStorageNames;
    private Context c;
    public FragmentRules() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.rules_fragment,container,false);
        myRecyclerView = myView.findViewById(R.id.recyclerview_rules);
        RecyclerViewAdapterRulesOverview adapter = new RecyclerViewAdapterRulesOverview(getContext(),myCategories, this);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myRecyclerView.setAdapter(adapter);
        return myView;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Database db = new Database(getContext());
        c = getContext();

        allCategories = new ArrayList<>(Arrays.asList("Spiele", "Kommunikation", "Social Media", "News", "Bildung", "Finanzen & Business", "Fitness & Gesundheit", "Essen & Drinks", "Lifestyle", "Andere"));
        allCategoriesStorageNames = new ArrayList<>(Arrays.asList("GAMES", "COMMUNICATION", "SOCIAL", "NEWS_AND_MAGAZINES", "PRODUCTIVITY", "BUSINESS", "HEALTH_AND_FITNESS", "FOOD_AND_DRINKS", "LIFESTYLE", "OTHERS"));
        allIcons = new ArrayList<>(Arrays.asList(c.getDrawable(R.drawable.games),c.getDrawable(R.drawable.message), c.getDrawable(R.drawable.socialmedia),
                c.getDrawable(R.drawable.news), c.getDrawable(R.drawable.education), c.getDrawable(R.drawable.money), c.getDrawable(R.drawable.fitness), c.getDrawable(R.drawable.food),
                c.getDrawable(R.drawable.socialmedia), c.getDrawable(R.drawable.more2)));
        String rules = db.getRulesFromInternalStorage("rules");
        myCategories = getCurrentCategorieObjects(rules);


    }

    public List<Category> getCurrentCategorieObjects(String rules){
        myCategories = new ArrayList<Category>();
        char[] myRules = rules.toCharArray();
        Log.e("Kristian Czepluch", myRules.toString());
        for(int i=0;i<myRules.length;i++) {
            if(myRules[i]=='0'){
                myCategories.add(new Category(allIcons.get(i),allCategories.get(i),false));
            } else {
                myCategories.add(new Category(allIcons.get(i),allCategories.get(i),true));
            }
        }
        return myCategories;
    }

    @Override
    public void onCategoryItemClicked(int position) {

        TinyDB tinyDB = new TinyDB(getContext());
        ArrayList<String> allPackagenames = tinyDB.getListString(allCategoriesStorageNames.get(position));

        PackageManager pm = getContext().getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<Anwendung> alleAnwendungen = new ArrayList<>();
        for(String packageName: allPackagenames){
            for(ApplicationInfo info: packages){
                if(info.packageName.equals(packageName)){
                    alleAnwendungen.add(new Anwendung(pm.getApplicationLabel(info).toString(), pm.getApplicationIcon(info)));
                }
            }
        }

        Applications_dialogFragment applications_dialogFragment = new Applications_dialogFragment();
        applications_dialogFragment.setData(alleAnwendungen, allCategories.get(position));
        applications_dialogFragment.show(getFragmentManager(),"App_Dialog");
    }
}
