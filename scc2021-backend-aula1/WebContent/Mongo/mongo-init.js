db.createUser(
        {
            user: "scc-afr",
            pwd: "secret",
            roles: [
                {
                    role: "readWrite",
                    db: "scc-afr-mongoDB"
                }
            ]
        }
);