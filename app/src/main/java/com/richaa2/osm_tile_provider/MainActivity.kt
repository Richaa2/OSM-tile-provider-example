package com.richaa2.osm_tile_provider

import android.content.res.AssetManager
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
import androidx.compose.ui.platform.LocalContext
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
import java.io.IOException
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
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            LatLng(
                41.498057819117015,
                -81.68799263298347
            ), 16f
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        MapEffect(Unit) { googleMap ->
            // Approach 1: Using an online tile provider (e.g., OSM)
            //
            // val osmTileProvider = OSMTileProvider()
            // val tileOverlayOptions = TileOverlayOptions().tileProvider(osmTileProvider)
            // googleMap.addTileOverlay(tileOverlayOptions)

            // Approach 2: Using a local tile provider from assets
            val tileProvider = LocalTileProvider(assetManager = context.assets)
            val tileOverlayOptions = TileOverlayOptions()
                .tileProvider(tileProvider)
            googleMap.addTileOverlay(tileOverlayOptions)
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
            Log.d("OSM_TILE", "Received data size: x $x y $y z $zoom ${data.size}")
            Tile(256, 256, data)
        } catch (e: Exception) {
            TileProvider.NO_TILE
        }
    }
}



/**
 * A TileProvider that loads tiles from the app's assets.
 *
 * The expected folder structure is:
 * assets/
 * └── {locationId}/
 *     └── {buildingId}/
 *         └── {floorId}/
 *             └── {zoom}/{x}/{y}.png
 */
class LocalTileProvider(
    private val assetManager: AssetManager,
    private val locationId: String = "",
    private val buildingId: String = "",
    private val floorId: String = ""
) : TileProvider {

    override fun getTile(x: Int, y: Int, zoom: Int): Tile {
        val tileSize = 256
        val filePath = "$locationId/$buildingId/$floorId/$zoom/$x/$y.png"
        return try {
            assetManager.open(filePath).use { inputStream ->
                val data = inputStream.readBytes()
                Log.d("LocalTileProvider", "Loaded tile: $filePath, size: ${data.size}")
                Tile(tileSize, tileSize, data)
            }
        } catch (e: IOException) {
            Log.e("LocalTileProvider", "Tile not found: $filePath", e)
            TileProvider.NO_TILE
        }
    }
}

