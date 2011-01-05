# Dashboard

This is a simple dashboard for a small team collaboration written in clojure using Compojure & MongoDB.
It's a first attempt to use Compojure for web develpment and it's in a very alpha state.
Lots of bugs and lacks of functionality.

## Usage

1. Install [Leiningen](https://github.com/technomancy/leiningen)
2. Install [MongoDB](http://www.mongodb.org/)
3. Install Dashboard:
        $ git clone git://github.com/jardev/dashboard.git
        $ cd dashboard
        $ make deps
        $ cp sites/template sites/default
4. Check settings in sites/default
5. Run:
        $ make start
6. Add a user:
        $ make repl
        user=> (require '[dashboard.db :as db])
        user=> (db/add-user {:username "demo"
                             :password "demo"
                             :email "your-email@demo.com"
                             :roles #{"user"}})
7. Got to http://127.0.0.1:8000/
8. Log in as demo/demo


## TODO
1. Roles
2. Invitation
3. ETA tags
4. Tasks
5. Edit ETA in 2 minutes after create
6. Users statistics
7. Tasks statistics
8. Reports

## License

Copyright (C) 2010 jardev

Distributed under the Eclipse Public License, the same as Clojure.
