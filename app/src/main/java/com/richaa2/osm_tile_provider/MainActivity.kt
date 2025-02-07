package com.richaa2.osm_tile_provider

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider
import com.google.android.gms.maps.model.UrlTileProvider
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import com.richaa2.osm_tile_provider.ui.theme.OSM_tile_providerTheme
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OSM_tile_providerTheme {
                MapScreen()
            }
        }
    }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapScreen() {
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            LatLng(
                41.499352497396636,
                -81.68899938708547
            ), 16f
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        MapEffect(Unit) {
            val osmTileProvider = OSMTileProvider()
            val tileOverlayOptions = TileOverlayOptions().tileProvider(osmTileProvider)
            it.addTileOverlay(tileOverlayOptions)
        }
    }
}
// Don`t forget to add the GOOGLE_MAPS_API_KEY in the AndroidManifest.xml file
class OSMTileProvider : TileProvider {

    private fun getTileUrl(x: Int, y: Int, zoom: Int): URL {
        val url = "https://tile.openstreetmap.org/$zoom/$x/$y.png"
        Log.d("OSM_TILE", "Requesting tile: $url")
        return URL(url)
    }

    override fun getTile(x: Int, y: Int, zoom: Int): Tile {
        return try {
            val connection = getTileUrl(x, y, zoom).openConnection()
            connection.setRequestProperty("User-Agent", "OSM-Android-App")

            val contentType = connection.getHeaderField("Content-Type")

            Log.d("OSM_TILE", "Content-Type: $contentType")

            val inputStream = connection.getInputStream()
            val data = inputStream.readBytes()
            Log.d("OSM_TILE", "Received data size: ${data.size}")
            Tile(256, 256, data)
        } catch (e: Exception) {
            TileProvider.NO_TILE
        }
    }
}

