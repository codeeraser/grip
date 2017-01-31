package de.metacode.grip

import groovy.util.logging.Slf4j
import ratpack.server.Service
import ratpack.server.StartEvent

/**
 * Created by mloesch on 30.01.17.
 */

@Slf4j
class GripService implements Service, Bootstrap {
    @Override
    void onStart(StartEvent event) throws Exception {
        run()
    }
}
