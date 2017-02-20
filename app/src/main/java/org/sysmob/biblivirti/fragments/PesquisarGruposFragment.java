package org.sysmob.biblivirti.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.sysmob.biblivirti.R;
import org.sysmob.biblivirti.adapters.PesquisaGruposFragmentAdapter;
import org.sysmob.biblivirti.adapters.PesquisarGruposAdapter;
import org.sysmob.biblivirti.application.BiblivirtiApplication;
import org.sysmob.biblivirti.model.Grupo;

import java.util.List;

/**
 * Created by micro99 on 17/02/2017.
 */
public class PesquisarGruposFragment extends Fragment {

    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private RecyclerView recyclerGrupos;
    private List<Grupo> grupos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pesquisar_grupos, container, false);

        this.layoutEmpty = (LinearLayout) view.findViewById(R.id.layoutEmpty);
        this.layoutEmpty.setVisibility(this.grupos == null ? View.VISIBLE : View.GONE);
        this.recyclerGrupos = (RecyclerView) view.findViewById(R.id.recyclerGrupos);
        new PesquisarGruposAdapter(getFragmentManager(), this.grupos);
        this.recyclerGrupos.setAdapter(new PesquisaGruposFragmentAdapter(getActivity(), this.grupos, BiblivirtiApplication.getInstance().getLoggedUser()));

        return view;
    }

    /*****************************************************
     * PUBLIC METHODS
     *****************************************************/
    public List<Grupo> getGrupos() {
        return grupos;
    }

    public void setGrupos(List<Grupo> grupos) {
        this.grupos = grupos;
    }
}
