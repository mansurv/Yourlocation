package com.netmontools.lookatnet.ui.local.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import static androidx.lifecycle.ViewModelProvider.*;

import com.netmontools.lookatnet.App;
import com.netmontools.lookatnet.R;
import com.netmontools.lookatnet.ui.local.viewmodel.LocalViewModel;
import com.netmontools.lookatnet.ui.local.model.Folder;
import com.netmontools.lookatnet.utils.SimpleUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class  LocalFragment extends Fragment {

    private static LocalViewModel localViewModel;
    private static LocalAdapter adapter;
    private static int position;

    private static final String ARG_COUNT = "param1";

    public LocalFragment() {
    }

    public static LocalFragment newInstance(int index) {
        LocalFragment fragment = new LocalFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COUNT, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
        setHasOptionsMenu(true);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_COUNT);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_local, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.local_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        adapter = new LocalAdapter();
        recyclerView.setAdapter(adapter);

        localViewModel = new AndroidViewModelFactory(App.getInstance()).create(LocalViewModel.class);
        localViewModel.getAllPoints().observe(getViewLifecycleOwner(), new Observer<List<Folder>>() {
            @Override
            public void onChanged(List<Folder> points) {
                adapter.setPoints(points);
            }
        });

        adapter.setOnItemClickListener(new LocalAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Folder point) {
                boolean isSelected = false;
                for (Folder folder : App.folders) {
                    if (folder.isChecked) {
                        if (point.isChecked) {
                            point.isChecked = false;
                        } else {
                            point.isChecked = true;
                        }
                        isSelected = true;
                        adapter.notifyItemChanged(position);
                        break;
                    }
                }

                if (!isSelected) {
                    if (!point.isFile) {
                        localViewModel.update(point);
                    } else {
                        try {
                            assert point.getPath() != null;
                            File file = new File(point.getPath());
                            if (file.exists() && (file.isFile())) {
                                SimpleUtils.openFile(App.instance, file);
                            }
                        } catch (NullPointerException npe) {
                            npe.getMessage();
                        }
                    }
                }
            }
        });

        adapter.setOnItemLongClickListener(new LocalAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Folder point) {
                if (point.isChecked) {
                    point.isChecked = false;
                } else {
                    point.isChecked = true;
                }
                adapter.notifyItemChanged(position);
            }
        });

        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OnBackPressedCallback callback = new OnBackPressedCallback(
                true // default to enabled
        ) {
            @Override
            public void handleOnBackPressed() {
                try {
                    File file = new File(App.previousPath);
                    if (!file.getPath().equalsIgnoreCase(App.rootPath)) {
                        if (file.exists()) {

                            file = new File(Objects.requireNonNull(file.getParent()));
                            Folder fd = new Folder();
                            fd.isFile = file.isFile();
                            fd.setName(file.getName());
                            fd.setPath(file.getPath());
                            if (fd.isFile) {
                                fd.setSize(file.length());
                                fd.setImage(App.file_image);
                            } else {
                                fd.setSize(0L);
                                fd.setImage(App.folder_image);
                            }
                            localViewModel.update(fd);

                            assert fd.getName() != null;
                            //if (fd.getName().equalsIgnoreCase(""))
                                //disabledText.setText(fd.getPath());
                            //else
                                //disabledText.setText(fd.getName());
                        }
                    } else {
                        this.setEnabled(false);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(
                this, // LifecycleOwner
                callback);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void deleteFolder() {
        assert getFragmentManager() != null;
        LocalFragment.confirmDelete.instantiate().show(getFragmentManager(), "confirm delete");
    }

    public static class confirmDelete extends DialogFragment {

        private static DialogFragment instantiate() { return new LocalFragment.confirmDelete();
        }

        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.confirm_title);
            builder.setMessage(R.string.confirm_message);
            builder.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            localViewModel.delete(adapter.getPointAt(position));
                            Toast.makeText(getActivity(), "File object deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            adapter.notifyItemChanged(position);
        }
    }
            );
            return builder.create();
        }
    };

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_local, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ret:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}