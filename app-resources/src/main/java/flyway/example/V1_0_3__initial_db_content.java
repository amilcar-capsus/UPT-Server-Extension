package flyway.example;

//import fi.nls.oskari.db.DBHandler;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.oskari.helpers.AppSetupHelper;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;

public class V1_0_3__initial_db_content extends BaseJavaMigration {

    public void migrate(Context context)
            throws Exception {
        // run setup based on json under /src/main/resources/setup/app-example.json
        AppSetupHelper.create(context, "app-example");
    }
}
