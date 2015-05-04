package de.metacode.grip.util

import javax.activation.DataSource

/**
 * Created by mloesch on 15.03.15.
 */
interface AttachmentProvider {
    DataSource toDataSource()
}
