package com.example.project_pendidikan;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_pendidikan.adapter.UserAdapter;
import com.example.project_pendidikan.db.DatabaseHelper;
import com.example.project_pendidikan.model.User;

import java.util.List;

public class UserListActivity extends AppCompatActivity {
    private RecyclerView recyclerViewUsers;
    private DatabaseHelper databaseHelper;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        databaseHelper = new DatabaseHelper(this);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        List<User> users = databaseHelper.getAllUsers();
        userAdapter = new UserAdapter(users);
        recyclerViewUsers.setAdapter(userAdapter);
    }
}
