/*
 *    Copyright 2019 James Fenn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.james.status.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.PreferenceData;
import com.james.status.data.preference.BasePreferenceData;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PreferenceSectionAdapter extends RecyclerView.Adapter<PreferenceSectionAdapter.ViewHolder> {

    private Context context;
    private List<BasePreferenceData.Identifier.SectionIdentifier> sections;
    private List<PreferenceAdapter> adapters;
    private List<BasePreferenceData> originalDatas, datas;

    public PreferenceSectionAdapter(Context context, List<BasePreferenceData> datas) {
        this.context = context;

        originalDatas = new ArrayList<>();
        originalDatas.addAll(datas);

        this.datas = new ArrayList<>();
        this.datas.addAll(originalDatas);

        sections = new ArrayList<>();
        adapters = new ArrayList<>();
        for (BasePreferenceData data : datas) {
            BasePreferenceData.Identifier.SectionIdentifier section = data.getIdentifier().getSection();
            if (!sections.contains(section)) {
                sections.add(section);
                adapters.add(new PreferenceAdapter(context, getItems(section)));
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_preference_section, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(sections.get(position).getName(context));

        ArrayList<BasePreferenceData> items = getItems(sections.get(position));
        PreferenceAdapter adapter = adapters.get(position);
        adapter.setItems(items);

        holder.recycler.setNestedScrollingEnabled(false);
        holder.recycler.setLayoutManager(new GridLayoutManager(context, 1));
        holder.recycler.setAdapter(adapter);

        if (items.size() > 0) holder.v.setVisibility(View.VISIBLE);
        else holder.v.setVisibility(View.GONE);

        holder.v.setAlpha(0);
        holder.v.animate().alpha(1).setDuration(500).start();
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    private ArrayList<BasePreferenceData> getItems(BasePreferenceData.Identifier.SectionIdentifier section) {
        ArrayList<BasePreferenceData> datas = new ArrayList<>();
        for (BasePreferenceData data : this.datas) {
            if (data.getIdentifier().getSection() == section) datas.add(data);
        }
        return datas;
    }

    public void notifyPreferenceChanged(PreferenceData... preferences) {
        for (PreferenceData preference : preferences) {
            for (int i = 0; i < adapters.size(); i++) {
                if (adapters.get(i).notifyPreferenceChanged(preference))
                    notifyItemChanged(i);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v;
        TextView title;
        RecyclerView recycler;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
            title = v.findViewById(R.id.title);
            recycler = v.findViewById(R.id.recycler);
        }
    }

    public void filter(@Nullable String string) {
        if (string != null && string.length() > 0) {
            ArrayList<BasePreferenceData> newDatas = new ArrayList<>();
            for (BasePreferenceData data : originalDatas) {
                BasePreferenceData.Identifier identifier = data.getIdentifier();

                String title = identifier.getTitle();
                if (title != null && title.toLowerCase().contains(string.toLowerCase())) {
                    newDatas.add(data);
                    continue;
                }

                String subtitle = identifier.getSubtitle();
                if (subtitle != null && subtitle.toLowerCase().contains(string.toLowerCase())) {
                    newDatas.add(data);
                    continue;
                }

                PreferenceData preference = identifier.getPreference();
                if (preference != null && preference.name().toLowerCase().contains(string)) {
                    newDatas.add(data);
                    continue;
                }

                BasePreferenceData.Identifier.SectionIdentifier section = identifier.getSection();
                if (section != null && string.contains(section.toString().toLowerCase())) {
                    newDatas.add(data);
                }
            }

            datas = newDatas;
        } else datas = originalDatas;
        notifyDataSetChanged();
    }
}
