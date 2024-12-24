package com.uds_improveit.preflistener.datasource_processor

import com.uds_improveit.preflistener.Builder
import kotlinx.coroutines.CoroutineScope

interface DataHandlerProcessor {

    fun setUpdateDataListener(listener: (data: Builder.UpdatesObject) -> Unit)

    fun setWorkerScope(scope: CoroutineScope)

}