package com.example.morta.where;

import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.firestore.auth.User;

import java.util.List;

public class User_position
{
    private XY_Place user;

    public User_position() {}

    public User_position(XY_Place user)
    {
        this.user = user;
    }

    public XY_Place getUser() {
        return user;
    }

    public void setUser(XY_Place user) {
        this.user = user;
    }
}
