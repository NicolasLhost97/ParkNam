package com.example.easyparknamur

//import com.mapbox.navigation.examples.R
//import com.mapbox.navigation.examples.databinding.MapboxActivityTurnByTurnExperienceBinding

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.easyparknamur.databinding.ActivityGpsBinding
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.layers.addLayerAbove
import com.mapbox.maps.extension.style.layers.generated.HeatmapLayer
import com.mapbox.maps.extension.style.layers.generated.heatmapLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maneuver.view.MapboxManeuverView
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.*
import com.mapbox.navigation.ui.tripprogress.view.MapboxTripProgressView
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import com.mapbox.navigation.ui.voice.model.SpeechError
import com.mapbox.navigation.ui.voice.model.SpeechValue
import com.mapbox.navigation.ui.voice.model.SpeechVolume
import org.json.JSONObject
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes


class GpsActivity : AppCompatActivity(), LocationListener{
//    private var baseUrl = "http://192.168.0.8:8000/"
    private var baseUrl = "http://192.168.106.113/"
    private lateinit var mapView: MapView
    protected var locationManager: LocationManager? = null
    protected var location: Location? = null
    private lateinit var selectedPoint: Point
    private var isPointThere: Boolean = false
    private var isHeatmap: Boolean = false
    private var isParked: Boolean = false
    private var isInNavigation: Boolean = false
    private var minDuration: Int = 0
    private var maxDuration = "4h"
    lateinit var annotationApi: AnnotationPlugin
    lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var pointAnnotation: PointAnnotation
    private lateinit var viewAnnotation: View
    private lateinit var mapboxMap: MapboxMap
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

    private val NO_BOUNDS: CameraBoundsOptions = CameraBoundsOptions.Builder()
        .bounds(
            CoordinateBounds(
                Point.fromLngLat(4.80, 50.5 ),
                Point.fromLngLat(4.92, 50.42 ),
                true
            )
        )
        .build()

    private companion object {
        private const val BUTTON_ANIMATION_DURATION = 1500L
        private const val FREE_PLACES_SOURCE_URL ="http://192.168.0.8:8000/place/predict/"
        private const val EARTHQUAKE_SOURCE_URL = "https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson"
        private const val FREE_PLACES_SOURCE_ID = "freePlaces"
        private const val HEATMAP_LAYER_ID = "freePlaces-heat"
        private const val HEATMAP_LAYER_SOURCE = "freePlaces"
        const val SELECTED_ADD_COEF_PX = 25
        val POINT: Point = Point.fromLngLat(4.8670029915, 50.4686871248)
    }

    /**
     * Debug tool used to play, pause and seek route progress events that can be used to produce mocked location updates along the route.
     */
    private val mapboxReplayer = MapboxReplayer()

    /**
     * Debug tool that mocks location updates with an input from the [mapboxReplayer].
     */
    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)

    /**
     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
     */
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)

    /**
     * Bindings to the example layout.
     */
    private lateinit var binding: ActivityGpsBinding

    /**
     * Used to execute camera transitions based on the data generated by the [viewportDataSource].
     * This includes transitions from route overview to route following and continuously updating the camera as the location changes.
     */
    private lateinit var navigationCamera: NavigationCamera

    /**
     * Produces the camera frames based on the location and routing data for the [navigationCamera] to execute.
     */
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    /*
     * Below are generated camera padding values to ensure that the route fits well on screen while
     * other elements are overlaid on top of the map (including instruction view, buttons, etc.)
     */
    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeOverviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            20.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeFollowingPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    /**
     * Generates updates for the [MapboxManeuverView] to display the upcoming maneuver instructions
     * and remaining distance to the maneuver point.
     */
    private lateinit var maneuverApi: MapboxManeuverApi

    /**
     * Generates updates for the [MapboxTripProgressView] that include remaining time and distance to the destination.
     */
    private lateinit var tripProgressApi: MapboxTripProgressApi

    /**
     * Generates updates for the [routeLineView] with the geometries and properties of the routes that should be drawn on the map.
     */
    private lateinit var routeLineApi: MapboxRouteLineApi

    /**
     * Draws route lines on the map based on the data from the [routeLineApi]
     */
    private lateinit var routeLineView: MapboxRouteLineView

    /**
     * Generates updates for the [routeArrowView] with the geometries and properties of maneuver arrows that should be drawn on the map.
     */
    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()

    /**
     * Draws maneuver arrows on the map based on the data [routeArrowApi].
     */
    private lateinit var routeArrowView: MapboxRouteArrowView

    /**
     * Stores and updates the state of whether the voice instructions should be played as they come or muted.
     */
    private var isVoiceInstructionsMuted = false
        set(value) {
            field = value
            if (value) {
                binding.soundButton.muteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(0f))
            } else {
                binding.soundButton.unmuteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(0.3f))
            }
        }

    /**
     * Extracts message that should be communicated to the driver about the upcoming maneuver.
     * When possible, downloads a synthesized audio file that can be played back to the driver.
     */
    private lateinit var speechApi: MapboxSpeechApi

    /**
     * Plays the synthesized audio files with upcoming maneuver instructions
     * or uses an on-device Text-To-Speech engine to communicate the message to the driver.
     */
    private lateinit var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer

    /**
     * Observes when a new voice instruction should be played.
     */
    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        speechApi.generate(voiceInstructions, speechCallback)
    }

    /**
     * Based on whether the synthesized audio file is available, the callback plays the file
     * or uses the fall back which is played back using the on-device Text-To-Speech engine.
     */
    private val speechCallback =
        MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
            expected.fold(
                { error ->
                    // play the instruction via fallback text-to-speech engine
                    voiceInstructionsPlayer.play(
                        error.fallback,
                        voiceInstructionsPlayerCallback
                    )
                },
                { value ->
                    // play the sound file from the external generator
                    voiceInstructionsPlayer.play(
                        value.announcement,
                        voiceInstructionsPlayerCallback
                    )
                }
            )
        }

    /**
     * When a synthesized audio file was downloaded, this callback cleans up the disk after it was played.
     */
    private val voiceInstructionsPlayerCallback =
        MapboxNavigationConsumer<SpeechAnnouncement> { value ->
            // remove already consumed file to free-up space
            speechApi.clean(value)
        }

    /**
     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
     * to the Maps SDK in order to update the user location indicator on the map.
     */
    private val navigationLocationProvider = NavigationLocationProvider()

    /**
     * Gets notified with location updates.
     *
     * Exposes raw updates coming directly from the location services
     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
     */
    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            // update location puck's position on the map
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )
            location = enhancedLocation

            if(!isHeatmap){
                if(!isInNavigation){
                    binding.search.visibility = View.VISIBLE
                }
                if(!isParked){
                    binding.parkedButton.visibility = View.VISIBLE
                }
            }
//            this.location = location
            if(location!!.speed<0.1f){
                binding.parkedButton.extend()
            } else {
                binding.parkedButton.shrink()
            }

            // update camera position to account for new location
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            // if this is the first location update the activity has received,
            // it's best to immediately move the camera to the current user location
            if (!firstLocationUpdateReceived) {
                firstLocationUpdateReceived = true
                navigationCamera.requestNavigationCameraToOverview(
                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                        .maxDuration(0) // instant transition
                        .build()
                )
            }
        }
    }

    /**
     * Gets notified with progress along the currently active route.
     */
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // update the camera position to account for the progressed fragment of the route
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        // draw the upcoming maneuver arrow on the map
        val style = binding.mapView.getMapboxMap().getStyle()
        if (style != null) {
            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

        // update top banner with maneuver instructions
        val maneuvers = maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold(
            { error ->
                Toast.makeText(
                    this@GpsActivity,
                    error.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            },
            {
                binding.maneuverView.visibility = View.VISIBLE
                binding.maneuverView.renderManeuvers(maneuvers)
            }
        )

        // update bottom trip progress summary
        binding.tripProgressView.render(
            tripProgressApi.getTripProgress(routeProgress)
        )
    }

    /**
     * Gets notified whenever the tracked routes change.
     *
     * A change can mean:
     * - routes get changed with [MapboxNavigation.setRoutes]
     * - routes annotations get refreshed (for example, congestion annotation that indicate the live traffic along the route)
     * - driver got off route and a reroute was executed
     */
    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
            // generate route geometries asynchronously and render them
            routeLineApi.setNavigationRoutes(
                routeUpdateResult.navigationRoutes
            ) { value ->
                binding.mapView.getMapboxMap().getStyle()?.apply {
                    routeLineView.renderRouteDrawData(this, value)
                }
            }

            // update the camera position to account for the new route
            viewportDataSource.onRouteChanged(routeUpdateResult.navigationRoutes.first())
            viewportDataSource.evaluate()
        } else {
            // remove the route line and route arrow from the map
            val style = binding.mapView.getMapboxMap().getStyle()
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(
                        style,
                        value
                    )
                }
                routeArrowView.render(style, routeArrowApi.clearArrows())
            }

            // remove the route reference from camera position evaluations
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
        }
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
                mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
                // start the trip session to being receiving location updates in free drive
                // and later when a route is set also receiving route progress updates
                mapboxNavigation.startTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
                mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
            }
        },
        onInitialize = this::initNavigation
    )

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGpsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapView = binding.mapView
        mapboxMap = binding.mapView.getMapboxMap()

        supportActionBar?.hide()

        annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager(mapView)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500,2f,this)

        if(ActivityCompat.checkSelfPermission(this@GpsActivity, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this@GpsActivity, Array<String>(1){Manifest.permission.ACCESS_FINE_LOCATION}, 100)
        }
        //Park button
        binding.parkedButton.shrink()
        binding.parkedButton.setOnClickListener {
            //sent infos
            sendPosition()
            if(isInNavigation){
                isInNavigation = false
                clearRouteAndStopNavigation()
                binding.search.visibility = View.VISIBLE
                binding.toolbar.backButton.visibility = View.INVISIBLE
                binding.toolbar.ToolbarTitle.text = "Navigation Libre"
            }
        }
        //Find Place Button
        binding.search.setOnClickListener {
            //calculate time between actual position and Namur
            requestTime()
            //Open Pannel with When and how much time
            showPredictionDialog()
        }

        //Info Button
        binding.toolbar.InfoButton.setOnClickListener{
            var dialog = Dialog(this@GpsActivity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_parking_info)
            var closeInfosButton = dialog.findViewById<View>(R.id.closeInfosButton)
            closeInfosButton.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

        //Launch Navigation to recommanded place
        binding.navigation.setOnClickListener {
            isHeatmap = false
            isInNavigation = true
            binding.mapView.getMapboxMap().loadStyleUri("mapbox://styles/nicocopops/cl9lnwtu600gn15ogwdcie57s") {mapboxMap.setBounds(NO_BOUNDS)}
            pointAnnotationManager.deleteAll()
            isPointThere = false
            findRoute(selectedPoint)
            binding.navigation.visibility = View.INVISIBLE
            binding.parkedButton.visibility = View.VISIBLE
            binding.toolbar.ToolbarTitle.text = "Navigation"
            binding.toolbar.backButton.visibility = View.INVISIBLE
        }

        //BackButton (only in heatmap)
        binding.toolbar.backButton.setOnClickListener {
            isHeatmap = false
            isInNavigation = false
            binding.mapView.getMapboxMap().loadStyleUri("mapbox://styles/nicocopops/cl9lnwtu600gn15ogwdcie57s") {mapboxMap.setBounds(NO_BOUNDS)}
            pointAnnotationManager.deleteAll()
            isPointThere = false
            binding.navigation.visibility = View.INVISIBLE
            binding.recenter.visibility = View.VISIBLE
            binding.NamurRecenter.visibility = View.VISIBLE
            binding.parkedButton.visibility = View.VISIBLE
            binding.toolbar.ToolbarTitle.text = "Navigation"
            binding.toolbar.backButton.visibility = View.INVISIBLE
        }

        //Add all parkings
        addParkingPoints()

        // initialize Navigation Camera
        viewportDataSource = MapboxNavigationViewportDataSource(binding.mapView.getMapboxMap())
        navigationCamera = NavigationCamera(
            binding.mapView.getMapboxMap(),
            binding.mapView.camera,
            viewportDataSource
        )
        // set the animations lifecycle listener to ensure the NavigationCamera stops
        // automatically following the user location when the map is interacted with
        binding.mapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )
        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
            // shows/hide the recenter button depending on the camera state
            if(!isHeatmap) {
                when (navigationCameraState) {
                    NavigationCameraState.TRANSITION_TO_FOLLOWING,
                    NavigationCameraState.FOLLOWING -> binding.recenter.visibility = View.INVISIBLE
                    NavigationCameraState.TRANSITION_TO_OVERVIEW,
                    NavigationCameraState.OVERVIEW,
                    NavigationCameraState.IDLE -> binding.recenter.visibility = View.VISIBLE
                }
            }
        }
        // set the padding values depending on screen orientation and visible view layout
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.overviewPadding = landscapeOverviewPadding
        } else {
            viewportDataSource.overviewPadding = overviewPadding
        }
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.followingPadding = landscapeFollowingPadding
        } else {
            viewportDataSource.followingPadding = followingPadding
        }

        // make sure to use the same DistanceFormatterOptions across different features
        val distanceFormatterOptions = DistanceFormatterOptions.Builder(this).build()

        // initialize maneuver api that feeds the data to the top banner maneuver view
        maneuverApi = MapboxManeuverApi(
            MapboxDistanceFormatter(distanceFormatterOptions)
        )

        // initialize bottom progress view
        tripProgressApi = MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(this)
                .distanceRemainingFormatter(
                    DistanceRemainingFormatter(distanceFormatterOptions)
                )
                .timeRemainingFormatter(
                    TimeRemainingFormatter(this)
                )
                .percentRouteTraveledFormatter(
                    PercentDistanceTraveledFormatter()
                )
                .estimatedTimeToArrivalFormatter(
                    EstimatedTimeToArrivalFormatter(this, TimeFormat.NONE_SPECIFIED)
                )
                .build()
        )

        // initialize voice instructions api and the voice instruction player
        speechApi = MapboxSpeechApi(
            this,
            getString(R.string.mapbox_access_token),
            Locale.FRANCE.language
        )
        voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(
            this,
            getString(R.string.mapbox_access_token),
            Locale.FRANCE.language
        )

        // initialize route line, the withRouteLineBelowLayerId is specified to place
        // the route line below road labels layer on the map
        // the value of this option will depend on the style that you are using
        // and under which layer the route line should be placed on the map layers stack
        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(this)
            .withRouteLineBelowLayerId("road-label-navigation")
            .build()
        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)

        // initialize maneuver arrow view to draw arrows on the map
        val routeArrowOptions = RouteArrowOptions.Builder(this).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)

        // load map style
        binding.mapView.getMapboxMap().loadStyleUri("mapbox://styles/nicocopops/cl9lnwtu600gn15ogwdcie57s") {
            // add long click listener that search for a route to the clicked destination
            binding.mapView.gestures.addOnMapLongClickListener { point ->
                if(isHeatmap){
                    addPoint(point)
                }
                true
            }
        }

        // initialize view interactions
        binding.stop.setOnClickListener {
            isInNavigation = false
            clearRouteAndStopNavigation()
            binding.search.visibility = View.VISIBLE
            binding.toolbar.backButton.visibility = View.INVISIBLE
            binding.toolbar.ToolbarTitle.text = "Navigation Libre"
        }
        binding.recenter.setOnClickListener {
            navigationCamera.requestNavigationCameraToFollowing()
            binding.routeOverview.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        binding.routeOverview.setOnClickListener {
            navigationCamera.requestNavigationCameraToOverview()
            binding.recenter.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        binding.soundButton.setOnClickListener {
            // mute/unmute voice instructions
            isVoiceInstructionsMuted = !isVoiceInstructionsMuted
        }
        binding.NamurRecenter.setOnClickListener {
            mapboxMap.flyTo(
                cameraOptions {
                    center(Point.fromLngLat(4.8625,50.465))
                    zoom(12.0) // Sets the zoom
                    bearing(0.0) // Rotate the camera
                    pitch(0.0) // Set the camera pitch
                },
            MapAnimationOptions.mapAnimationOptions {
                duration(2500)
            }
            )
        }
        Handler().postDelayed({
            binding.NamurRecenter.shrink()
        }, 3000)

        // set initial sounds button state
        binding.soundButton.unmute()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxReplayer.finish()
        maneuverApi.cancel()
        routeLineApi.cancel()
        routeLineView.cancel()
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
    }

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                // comment out the location engine setting block to disable simulation
//                .locationEngine(replayLocationEngine)
                .build()
        )

        // initialize location puck
        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            this.locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    this@GpsActivity,
                    R.drawable.mapbox_navigation_puck_icon
                )
            )
            enabled = true
        }

        replayOriginLocation()
    }

    private fun replayOriginLocation() {
        mapboxReplayer.pushEvents(
            listOf(
                ReplayRouteMapper.mapToUpdateLocation(
                    Date().time.toDouble(),
//                    Point.fromLngLat(location!!.longitude, location!!.latitude)
                    point = Point.fromLngLat(4.86746, 50.4669)
                )
            )
        )
        mapboxReplayer.playFirstLocation()
        mapboxReplayer.playbackSpeed(3.0)
    }

    private fun findRoute(destination: Point) {
        val originLocation = navigationLocationProvider.lastLocation
        val originPoint = originLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return

        // execute a route request
        // it's recommended to use the
        // applyDefaultNavigationOptions and applyLanguageAndVoiceUnitOptions
        // that make sure the route request is optimized
        // to allow for support of all of the Navigation SDK features
        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .coordinatesList(listOf(originPoint, destination))
                // provide the bearing for the origin of the request to ensure
                // that the returned route faces in the direction of the current user movement
                .bearingsList(
                    listOf(
                        Bearing.builder()
                            .angle(originLocation.bearing.toDouble())
                            .degrees(45.0)
                            .build(),
                        null
                    )
                )
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
                .build(),
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                    // no impl
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    // no impl
                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    setRouteAndStartNavigation(routes)
                }
            }
        )
    }

    private fun setRouteAndStartNavigation(routes: List<NavigationRoute>) {
        // set routes, where the first route in the list is the primary route that
        // will be used for active guidance
        mapboxNavigation.setNavigationRoutes(routes)

        // show UI elements
        binding.soundButton.visibility = View.VISIBLE
        binding.routeOverview.visibility = View.VISIBLE
        binding.tripProgressCard.visibility = View.VISIBLE
        binding.NamurRecenter.visibility = View.INVISIBLE

        // move the camera to overview when new route is available
        navigationCamera.requestNavigationCameraToOverview()
    }

    private fun clearRouteAndStopNavigation() {
        // clear
        mapboxNavigation.setNavigationRoutes(listOf())

        // stop simulation
        mapboxReplayer.stop()

        // hide UI elements
        binding.soundButton.visibility = View.INVISIBLE
        binding.maneuverView.visibility = View.INVISIBLE
        binding.routeOverview.visibility = View.INVISIBLE
        binding.tripProgressCard.visibility = View.INVISIBLE
        binding.NamurRecenter.visibility = View.VISIBLE
    }

    override fun onLocationChanged(location: Location) {
        binding.parkedButton.visibility = View.VISIBLE
        isParked = false
    }

    fun addPoint(point: Point){
        //Only one point
        if(isPointThere){
            pointAnnotationManager.deleteAll()
        }
        selectedPoint = point
        //Create point on map
        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(Point.fromLngLat(point.longitude(), point.latitude()))
            .withIconImage(getBitmap(R.drawable.markerheat))
        pointAnnotationManager.create(pointAnnotationOptions)
        isPointThere = true
        //Ask for navigation
        binding.navigation.visibility = View.VISIBLE
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

    private fun showPredictionDialog(){
        var calendar = GregorianCalendar.getInstance()
        calendar.time = Date()
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        var min = calendar.get(Calendar.MINUTE)
        var dialog = Dialog(this@GpsActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_ask_prediction)
        //Spinner
        var spinner = dialog.findViewById<TimePicker>(R.id.spinner)
        spinner.hour = hour
        spinner.minute = min
        spinner.setIs24HourView(true)
        spinner.setOnTimeChangedListener(TimePicker.OnTimeChangedListener { timePicker, spinHour, spinMinute ->
            hour = spinHour
            min = spinMinute
        })
        //Now and LaterButton
        var nowButton = dialog.findViewById<Button>(R.id.now)
        var laterButton = dialog.findViewById<Button>(R.id.later)
        var isNow = true
        nowButton.setOnClickListener{
            nowButton.setBackgroundColor(Color.parseColor("#D8D8D8"))
            laterButton.setBackgroundColor(Color.parseColor("#FFFFFF"))
            spinner.visibility= View.GONE
            isNow = true
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            min = calendar.get(Calendar.MINUTE)
            spinner.hour = hour
            spinner.minute = min
        }
        laterButton.setOnClickListener{
            laterButton.setBackgroundColor(Color.parseColor("#D8D8D8"))
            nowButton.setBackgroundColor(Color.parseColor("#FFFFFF"))
            spinner.visibility= View.VISIBLE
            isNow = false
        }
        maxDuration = "4h"
        //Max Time button
        var min30Button = dialog.findViewById<View>(R.id.button30min)
        var h3Button = dialog.findViewById<View>(R.id.button3h)
        var h4Button = dialog.findViewById<View>(R.id.button4h)
        var h8Button = dialog.findViewById<View>(R.id.button8h)
        min30Button.setOnClickListener{
            min30Button.setBackgroundColor(Color.parseColor("#D8D8D8"))
            h3Button.setBackgroundColor(Color.parseColor("#FFFFFF"))
            h4Button.setBackgroundColor(Color.parseColor("#FFFFFF"))
            h8Button.setBackgroundColor(Color.parseColor("#FFFFFF"))
            maxDuration = "30min"
        }
        h3Button.setOnClickListener{
            h3Button.setBackgroundColor(Color.parseColor("#D8D8D8"))
            min30Button.setBackgroundColor(Color.parseColor("#FFFFFF"))
            h4Button.setBackgroundColor(Color.parseColor("#FFFFFF"))
            h8Button.setBackgroundColor(Color.parseColor("#FFFFFF"))
            maxDuration = "3h"
        }
        h4Button.setOnClickListener{
            h4Button.setBackgroundColor(Color.parseColor("#D8D8D8"))
            min30Button.setBackgroundColor(Color.parseColor("#FFFFFF"))
            h3Button.setBackgroundColor(Color.parseColor("#FFFFFF"))
            h8Button.setBackgroundColor(Color.parseColor("#FFFFFF"))
            maxDuration = "4h"
        }
        h8Button.setOnClickListener{
            h8Button.setBackgroundColor(Color.parseColor("#D8D8D8"))
            min30Button.setBackgroundColor(Color.parseColor("#FFFFFF"))
            h3Button.setBackgroundColor(Color.parseColor("#FFFFFF"))
            h4Button.setBackgroundColor(Color.parseColor("#FFFFFF"))
            maxDuration = "8h"
        }
        //Validation Button
        var validationButton = dialog.findViewById<View>(R.id.predictionValidation)
        validationButton.setOnClickListener{
            launchPredictionMap(hour, min)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun launchPredictionMap(hour: Int, min: Int){
        //Calculate hour in Namur
        var hourTrajet = minDuration/60
        var minTrajet = minDuration%60

        var minAtNamur = min + minTrajet
        var extraHour = minAtNamur/60
        minAtNamur = minAtNamur%60
        var hourAtNamur = hour + hourTrajet + extraHour
        var extraDays = hourAtNamur/24
        hourAtNamur = hourAtNamur%24
        //send Infos
        var calendar = GregorianCalendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DATE,+extraDays)
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH), hourAtNamur, minAtNamur)
        var dateFormat = calendar.get(Calendar.YEAR).toString()+"-"+(calendar.get(Calendar.MONTH)+1).toString()+"-"+calendar.get(Calendar.DAY_OF_MONTH).toString()+"_"+hourAtNamur.toString()+":"+minAtNamur.toString()+":00"

//        requestHeatmap(dateFormat)

        //show heatmap
        mapboxMap.flyTo(
            cameraOptions {
                center(Point.fromLngLat(4.866,50.465))
                zoom(13.0) // Sets the zoom
                bearing(0.0) // Rotate the camera
                pitch(0.0) // Set the camera pitch
            },
            MapAnimationOptions.mapAnimationOptions {
                duration(2500)
            }
        )
        isHeatmap = true;
//        Handler().postDelayed({
            //mapbox://styles/nicocopops/clapesx4w002b14ktq95094o7
            binding.mapView.getMapboxMap().loadStyleUri("mapbox://styles/nicocopops/clar1uudt007h15p6ywwpatat") { style ->
                addRuntimeLayers(style, dateFormat)
                binding.mapView.gestures.addOnMapLongClickListener { point ->
                    if(isHeatmap){
                        addPoint(point)
                    }
                    true
                }
            }
//        }, 1500)
//        binding.mapView.getMapboxMap().apply {
//            loadStyleUri("mapbox://styles/nicocopops/clar1uudt007h15p6ywwpatat") { style -> addRuntimeLayers(style) }
//        }
        //Hide all unecessary infos
        binding.recenter.visibility = View.INVISIBLE
        binding.search.visibility = View.INVISIBLE
        binding.parkedButton.visibility = View.INVISIBLE
        binding.NamurRecenter.visibility = View.INVISIBLE

        binding.toolbar.backButton.visibility = View.VISIBLE
        binding.toolbar.ToolbarTitle.text = "Disponibilité probable des places"
    }

    private fun addRuntimeLayers(style: Style, date: String) {
        style.addSource(createHeatmapSource(date))
        style.addLayerAbove(createHeatmapLayer(), "waterway-label")
    }

    private fun createHeatmapSource(date: String): GeoJsonSource {
        return geoJsonSource(FREE_PLACES_SOURCE_ID) {
//            Log.e("geoJsonSource", url(FREE_PLACES_SOURCE_URL+date).sourceId)
            url(FREE_PLACES_SOURCE_URL+date)
        }
    }

    private fun addParkingPoints(){

    }





    private fun requestTime(){
        var url = "https://api.mapbox.com/optimized-trips/v1/mapbox/driving/"+location?.longitude+","+location?.latitude+";"+"4.8651,50.4649?access_token="+getString(R.string.mapbox_access_token)
        var requestQueue: RequestQueue = Volley.newRequestQueue(this@GpsActivity)
        var objectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                val trip = response.getJSONArray("trips").get(0) as JSONObject
                val fastestTrip = trip.getJSONArray("legs").get(0) as JSONObject
                val duration = fastestTrip.get("duration")
                if(duration is Int){
                    minDuration = (duration / 60)
                } else if (duration is Double){
                    minDuration = (duration / 60).toInt()
                }
                Log.e("RESPONSE", minDuration.toString())
            },
            { error ->
                Log.e("ERROR", error.toString())
            }
        )
        requestQueue.add(objectRequest)
    }

    private fun requestHeatmap(date: String){
        var url = baseUrl+"place/predict/"+date
        var requestQueue: RequestQueue = Volley.newRequestQueue(this@GpsActivity)
        var objectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
//                val trip = response.getJSONArray("trips").get(0) as JSONObject
                Log.e("RESPONSE", response.toString())
            },
            { error ->
                Log.e("ERROR", error.toString())
            }
        )
        requestQueue.add(objectRequest)
    }

    private fun sendPosition(){
        var url = baseUrl+"place/park?x="+location?.latitude.toString()+"&y="+location?.longitude.toString()+"&stay="+maxDuration
//        var url = baseUrl+"place/park?x=50.46593932751004&y=4.875642089405123&stay=30min"
        var requestQueue: RequestQueue = Volley.newRequestQueue(this@GpsActivity)
        var objectRequest: StringRequest = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->
                if(!response.equals("\"\"")){
                    var lat = response.substringBefore(",").substringAfter("\"").substringBefore("\"")
                    var long = response.substringAfter(",").substringAfter("\"").substringBefore("\"")
//                    Log.e("Lat long", lat+" and "+long)
                    mapboxMap.flyTo(
                        cameraOptions {
                            center(Point.fromLngLat(long.toDouble(),lat.toDouble()))
                            zoom(18.5) // Sets the zoom
                            bearing(0.0) // Rotate the camera
                            pitch(0.0) // Set the camera pitch
                        },
                        MapAnimationOptions.mapAnimationOptions {
                            duration(2500)
                        })

                    Handler().postDelayed({
                        mapboxMap.flyTo(
                            cameraOptions {
                                center(Point.fromLngLat(location!!.longitude,location!!.latitude))
                                zoom(16.0) // Sets the zoom
                                bearing(0.0) // Rotate the camera
                                pitch(0.0) // Set the camera pitch
                            },
                            MapAnimationOptions.mapAnimationOptions {
                                duration(2500)
                            })
                    }, 4000)
                }
                Toast.makeText(this@GpsActivity, "Votre emplacement a bien été enregistré!",Toast.LENGTH_SHORT).show()
                //Hide button until location change for more than 2m
                binding.parkedButton.visibility = View.INVISIBLE
                isParked = true
            },
            { error ->
                Log.e("ERROR", error.toString())
            }
        ){
//           override fun getParams():Map<String, String> {
//               var params: MutableMap<String, String> = HashMap()
//               params["latitude"] = location?.latitude.toString()
//               params["longitude"] = location?.longitude.toString()
//               params["duration"] = maxDuration
//               return params
//           }
        }
        requestQueue.add(objectRequest)
    }

    private fun createHeatmapLayer(): HeatmapLayer {
        return heatmapLayer(
            HEATMAP_LAYER_ID,
            FREE_PLACES_SOURCE_ID
        ) {
            maxZoom(20.0)
            sourceLayer(HEATMAP_LAYER_SOURCE)
            // Begin color ramp at 0-stop with a 0-transparancy color
            // to create a blur-like effect.
            heatmapColor(
                interpolate {
                    linear()
                    heatmapDensity()
                    stop {
                        literal(0)
                        rgba(0.0, 0.0, 255.0, 0.0)
                    }
                    stop {
                        literal(0.1)
                        rgb(65.0, 105.0, 225.0)
                    }
                    stop {
                        literal(0.3)
                        rgb(0.0, 255.0, 255.0)
                    }
                    stop {
                        literal(0.5)
                        rgb(0.0, 255.0, 0.0)
                    }
                    stop {
                        literal(0.7)
                        rgb(255.0, 255.0, 0.0)
                    }
                    stop {
                        literal(1)
                        rgb(255.0, 0.0, 0.0)
                    }
                }
            )
            // Increase the heatmap weight based on frequency and property magnitude
            heatmapWeight(
                interpolate {
                    linear()
                    get { literal("mag") }
                    stop {
                        literal(0)
                        literal(0)
                    }
                    stop {
                        literal(6)
                        literal(1)
                    }
                }
            )
            // Increase the heatmap color weight weight by zoom level
            // heatmap-intensity is a multiplier on top of heatmap-weight
            heatmapIntensity(
                interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0)
                        literal(1)
                    }
                    stop {
                        literal(9)
                        literal(3)
                    }
                }
            )
            // Adjust the heatmap radius by zoom level
            heatmapRadius(
                interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0)
                        literal(2)
                    }
                    stop {
                        literal(9)
                        literal(20)
                    }
                }
            )
            // Opacity for zoom level
            heatmapOpacity(
                interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(1)
                        literal(0.65)
                    }
                }
            )
        }
    }
}