package com.example.easyparknamur

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.easyparknamur.databinding.ActivityPlaceFinderBinding
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures


class PlaceFinderActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var binding: ActivityPlaceFinderBinding
    lateinit var annotationApi: AnnotationPlugin
    lateinit var pointAnnotationManager: PointAnnotationManager
    private var isPointThere: Boolean = false
    private val NAMUR_BOUND: CameraBoundsOptions = CameraBoundsOptions.Builder()
        .bounds(
            CoordinateBounds(
                Point.fromLngLat(4.80, 50.5 ),
                Point.fromLngLat(4.92, 50.42 ),
                false
            )
        )
        .minZoom(8.0)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceFinderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapboxMap = binding.mapView.getMapboxMap()
        mapView = binding.mapView
        annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager(mapView)

        //"mapbox://styles/nicocopops/clapesx4w002b14ktq95094o7"
        binding.mapView.getMapboxMap().loadStyleUri(intent.extras!!.get("map").toString()) {
            mapboxMap.setBounds(NAMUR_BOUND)
            binding.mapView.gestures.addOnMapLongClickListener { point ->
                addPoint(point)
                true
            }
        }
    }


    fun addPoint(point: Point){
        //Only one point
        if(isPointThere){
            pointAnnotationManager?.deleteAll()
        }
        //Create point on map
        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(Point.fromLngLat(point.longitude(), point.latitude()))
            .withIconImage(getBitmap(R.drawable.mapmarker))
        pointAnnotationManager?.create(pointAnnotationOptions)
        //Ask for navigation
    }
















    fun addPointBis(point: Point){
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        bitmapFromDrawableRes(
            this@PlaceFinderActivity,
            R.drawable.mapmarker
        )?.let {
            val annotationApi = mapView?.annotations
            val pointAnnotationManager = annotationApi?.createPointAnnotationManager(mapView!!)
// Set options for the resulting symbol layer.
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
// Define a geographic coordinate.
                .withPoint(Point.fromLngLat(point.longitude(), point.latitude()))
// Specify the bitmap you assigned to the point annotation
// The bitmap will be added to map style automatically.
                .withIconImage(it)
// Add the resulting pointAnnotation to the map.
            pointAnnotationManager?.create(pointAnnotationOptions)
        }
    }

    private fun getBitmap(drawableRes: Int): Bitmap {
        val drawable = resources.getDrawable(drawableRes)
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
// copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }
}