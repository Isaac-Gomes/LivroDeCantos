package com.example.livrodecantos.cifra

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.livrodecantos.data.repository.PreferenciasCifraRepository
import com.example.livrodecantos.model.cifra.ConfiguracaoCifra
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreferenciasCifraRepositoryTest {
    @Test
    fun configuracoesSaoPersistidasPorCantoERestauradas() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val arquivo = File(context.cacheDir, "cifra-${UUID.randomUUID()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = escopo) { arquivo }
        val repositorio = PreferenciasCifraRepository(dataStore)

        try {
            assertEquals(ConfiguracaoCifra(), repositorio.observarConfiguracao(1).first())
            assertEquals(0, repositorio.observarSemitons(1).first())
            assertEquals(0, repositorio.observarCapotraste(1).first())

            repositorio.salvarSemitons(1, 2)
            repositorio.salvarCapotraste(1, 2)
            assertEquals(ConfiguracaoCifra(semitons = 2, capotraste = 2),
                repositorio.observarConfiguracao(1).first())

            repositorio.salvarSemitons(2, -3)
            repositorio.salvarCapotraste(2, 7)
            assertEquals(ConfiguracaoCifra(semitons = 2, capotraste = 2),
                repositorio.observarConfiguracao(1).first())
            assertEquals(ConfiguracaoCifra(semitons = -3, capotraste = 7),
                repositorio.observarConfiguracao(2).first())

            repositorio.restaurarConfiguracao(1)
            assertEquals(ConfiguracaoCifra(), repositorio.observarConfiguracao(1).first())
        } finally {
            escopo.cancel()
        }
    }
}
