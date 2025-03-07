package com.mco.accessability.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mco.accessability.DataHelper
import com.mco.accessability.R
import com.mco.accessability.SharedViewModel
import com.mco.accessability.adapter.DialogPostAdapter
import com.mco.accessability.adapter.SuggestionsAdapter
import com.mco.accessability.databinding.BottomDialogBinding
import com.mco.accessability.databinding.FragmentMapBinding
import com.mco.accessability.models.MarkerData
import com.mco.accessability.models.UserRating


import com.mco.accessability.models.ReviewModel

class MapFragment : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var isMarkerAdderModeEnabled = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val markers: MutableList<Marker> = mutableListOf()
    private lateinit var database: DatabaseReference
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var auth: FirebaseAuth // Add FirebaseAuth
    private lateinit var bottomDialogBinding: BottomDialogBinding
    private lateinit var dialogPostAdapter: DialogPostAdapter

    private lateinit var mapbinding: FragmentMapBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var suggestionsAdapter: SuggestionsAdapter


    // Access the same SharedViewModel as the activity
    //private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Get Database Reference
        auth = FirebaseAuth.getInstance() // Initialize FirebaseAuth
        database = FirebaseDatabase.getInstance().getReference("markers")

        // MapBinding
        mapbinding = FragmentMapBinding.inflate(inflater)
        val view = mapbinding.root
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Recycler view for suggestions
        recyclerView = mapbinding.suggestionsRecyclerView

        suggestionsAdapter = SuggestionsAdapter(emptyList()) { selectedMarker ->
            // Handle marker click
            val markerPosition = LatLng(selectedMarker.lat, selectedMarker.lng)
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 15f))
            recyclerView.visibility = View.GONE
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = suggestionsAdapter

        //val markerAdderButton: ImageView = view.findViewById(R.id.markerAdderModeButton)

        // Find the marker adder mode button and set the click listener
        val markerAdderButton: ImageView = mapbinding.markerAdderModeButton

        markerAdderButton.setOnClickListener {
            isMarkerAdderModeEnabled = !isMarkerAdderModeEnabled
            if (isMarkerAdderModeEnabled) {
                markerAdderButton.setImageResource(R.drawable.cancel_add_marker)
            } else {
                markerAdderButton.setImageResource(R.drawable.rate_location_icon)
            }
        }

        // Find zoom buttons and set their click listeners
        val zoomInButton: Button = mapbinding.zoomInButton
        val zoomOutButton: Button = mapbinding.zoomOutButton
        //val zoomInButton: Button = view.findViewById(R.id.zoomInButton)
        //val zoomOutButton: Button = view.findViewById(R.id.zoomOutButton)
        zoomInButton.setOnClickListener { googleMap?.animateCamera(CameraUpdateFactory.zoomIn()) }
        zoomOutButton.setOnClickListener { googleMap?.animateCamera(CameraUpdateFactory.zoomOut()) }

        //val searchEditText: EditText = view.findViewById(R.id.searchMarkerEditText)
        // Find the search EditText and set a listener
        val searchEditText: EditText = mapbinding.searchMarkerEditText
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSuggestions(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //sharedViewModel.logMarkerData()
        return view
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d("MapFragment", "Map is ready")
        googleMap = map
        requestLocationPermission()

        googleMap?.setOnMapClickListener { latLng ->
            if (isMarkerAdderModeEnabled) {
                showAddMarkerDialog(latLng)
            } else {
                googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            }
        }

        googleMap?.setOnMarkerClickListener { marker ->
            val markerId = marker.tag as? String
            markerId?.let { fetchMarkerDetails(it) }
            true
        }

        loadMarkersFromFirebase()
    }

    private fun showAddMarkerDialog(latLng: LatLng) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(requireContext(), "You need to log in to add a marker", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserEmail = currentUser.email ?: "Anonymous"

        getUsernameFromFirestore(currentUserEmail) { username ->
            if (username == null) {
                Toast.makeText(requireContext(), "Failed to retrieve username", Toast.LENGTH_SHORT).show()
                return@getUsernameFromFirestore
            }

            val dialogView = layoutInflater.inflate(R.layout.add_marker_details_pop_up, null)
            val etMarkerName = dialogView.findViewById<EditText>(R.id.et_marker_name)
            val etNote = dialogView.findViewById<EditText>(R.id.et_note)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create()

            dialogView.findViewById<Button>(R.id.btn_confirm).setOnClickListener {
                val markerName = etMarkerName.text.toString().ifBlank { "New Marker" }
                val noteText = etNote.text.toString()

                // Get the selected rating from the RadioGroup
                val selectedRating = dialogView.findViewById<RadioGroup>(R.id.rg_rating)
                    .checkedRadioButtonId.let { id ->
                        dialogView.findViewById<RadioButton>(id).text.toString().toInt()
                    }

                val db = FirebaseFirestore.getInstance()
                db.collection("marker")
                    .whereEqualTo("nameOfPlace", markerName)
                    .get()
                    .addOnSuccessListener { result ->
                        if (result.isEmpty) {
                            // If marker does not exist, create a new one
                            val markerData = MarkerData(
                                nameOfPlace = markerName,
                                lat = latLng.latitude,
                                lng = latLng.longitude,
                                imageres = R.drawable.placeholder,
                                ratings = listOf(UserRating(username, selectedRating)) // Replace Pair with UserRating
                            )

                            if (noteText.isNotBlank()) {
                                val review = ReviewModel(
                                    author = username,
                                    notes = noteText,
                                    imageId = R.drawable.placeholder,
                                    rating = selectedRating
                                )

                                db.collection("review").add(review).addOnSuccessListener { reviewRef ->
                                    val reviewId = reviewRef.id
                                    markerData.notes = listOf(reviewId)

                                    db.collection("marker").add(markerData).addOnSuccessListener { markerRef ->
                                        Log.d("MapFragment", "Marker added with ID: ${markerRef.id}")
                                        addMarkerToMap(markerData, markerRef.id)
                                        dialog.dismiss()
                                    }.addOnFailureListener { e ->
                                        Toast.makeText(requireContext(), "Failed to add marker", Toast.LENGTH_SHORT).show()
                                        Log.e("MapFragment", "Failed to add marker: ${e.message}")
                                    }
                                }.addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Failed to add review", Toast.LENGTH_SHORT).show()
                                    Log.e("MapFragment", "Failed to add review: ${e.message}")
                                }
                            } else {
                                db.collection("marker").add(markerData).addOnSuccessListener { markerRef ->
                                    Log.d("MapFragment", "Marker added with ID: ${markerRef.id}")
                                    addMarkerToMap(markerData, markerRef.id)
                                    dialog.dismiss()
                                }.addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Failed to add marker", Toast.LENGTH_SHORT).show()
                                    Log.e("MapFragment", "Failed to add marker: ${e.message}")
                                }
                            }
                        } else {
                            // If marker exists, update ratings
                            val document = result.documents.first()
                            val markerData = document.toObject(MarkerData::class.java)

                            if (markerData != null) {
                                val updatedRatings = markerData.ratings + UserRating(username, selectedRating) // Replace Pair with UserRating
                                document.reference.update("ratings", updatedRatings).addOnSuccessListener {
                                    Log.d("MapFragment", "Rating updated for marker: $markerName")
                                    dialog.dismiss()
                                }.addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Failed to update ratings", Toast.LENGTH_SHORT).show()
                                    Log.e("MapFragment", "Failed to update ratings: ${e.message}")
                                }
                            }
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to fetch marker data", Toast.LENGTH_SHORT).show()
                        Log.e("MapFragment", "Failed to fetch marker data: ${e.message}")
                    }
            }

            dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }





    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            enableUserLocation()
        }
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLocation = LatLng(it.latitude, it.longitude)
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                }
            }
        }
    }

    private fun loadMarkersFromFirebase() {
        Log.d("MapFragment", "Loading markers from Firebase...") // Log the function call
        val db = FirebaseFirestore.getInstance()

        db.collection("marker")
            .get()
            .addOnSuccessListener { result ->
                markers.clear() // Clear any existing markers on the map
                Log.d("MapFragment", "Markers fetched: ${result.size()}")
                for (document in result) {
                    val markerData = document.toObject(MarkerData::class.java)
                    markerData?.let {
                        addMarkerToMap(it, document.id)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MapFragment", "Error loading markers: ${exception.message}")
                Toast.makeText(requireContext(), "Failed to load markers", Toast.LENGTH_SHORT).show()
            }
    }


    private fun addMarkerToMap(markerData: MarkerData, markerId: String?) {
        val markerOptions = MarkerOptions()
            .position(LatLng(markerData.lat, markerData.lng))
            .title(markerData.nameOfPlace)

        val marker = googleMap?.addMarker(markerOptions)
        marker?.tag = markerId
        marker?.let { markers.add(it) }
    }


    private fun fetchMarkerDetails(markerId: String) {
        Log.d("MapFragment", "fetchMarkerDetails")

        val db = FirebaseFirestore.getInstance()
        db.collection("marker")  // Reference to the "markers" collection
            .document(markerId)    // Reference to the specific marker document
            .get()  // Fetch the document
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val markerData = document.toObject(MarkerData::class.java)
                    markerData?.let { showMarkerPopup(it) }
                } else {
                    Toast.makeText(requireContext(), "Marker not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to fetch marker details: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showMarkerPopup(markerData: MarkerData) {
        Log.d("MapFragment", "showMarkerPopup")

        bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_dialog, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        bottomSheetView.findViewById<TextView>(R.id.markername).text = markerData.nameOfPlace

        val averageRating = if (markerData.ratings.isNotEmpty()) {
            markerData.ratings.map { it.rating }.average().toFloat()
        } else {
            0f
        }
        bottomSheetView.findViewById<TextView>(R.id.rate).text = String.format("%.1f", averageRating)

        val recyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.rcv_dialog)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = DialogPostAdapter(arrayListOf())
        recyclerView.adapter = adapter

        loadReviews(markerData, adapter)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val currentUserEmail = currentUser?.email ?: "Anonymous"

        // Fetch username and set the checked radio button
        getUsernameFromFirestore(currentUserEmail) { username ->
            if (username != null) {
                // Find the current user's rating
                val userRating = markerData.ratings.find { it.username == username }?.rating ?: 0

                // Check the corresponding radio button
                val radioGroup = bottomSheetView.findViewById<RadioGroup>(R.id.rg_rating)
                when (userRating) {
                    1 -> bottomSheetView.findViewById<RadioButton>(R.id.rb_star_1).isChecked = true
                    2 -> bottomSheetView.findViewById<RadioButton>(R.id.rb_star_2).isChecked = true
                    3 -> bottomSheetView.findViewById<RadioButton>(R.id.rb_star_3).isChecked = true
                    4 -> bottomSheetView.findViewById<RadioButton>(R.id.rb_star_4).isChecked = true
                    5 -> bottomSheetView.findViewById<RadioButton>(R.id.rb_star_5).isChecked = true
                }

                // Handle new rating selection
                radioGroup.setOnCheckedChangeListener { _, checkedId ->
                    val newRating = when (checkedId) {
                        R.id.rb_star_1 -> 1
                        R.id.rb_star_2 -> 2
                        R.id.rb_star_3 -> 3
                        R.id.rb_star_4 -> 4
                        R.id.rb_star_5 -> 5
                        else -> 0
                    }
                    if (newRating != 0) {
                        updateUserRating(markerData, username, newRating)
                    }
                }
            } else {
                Log.d("MapFragment", "Username not found")
            }
        }

        bottomSheetView.findViewById<Button>(R.id.btnAddReview).setOnClickListener {
            handleAddReview(markerData, bottomSheetView, adapter)
        }

        bottomSheetDialog.show()
    }

    private fun updateUserRating(markerData: MarkerData, username: String, newRating: Int) {

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null || username == "Anonymous") {
            Toast.makeText(requireContext(), "You need to log in to add or update rating", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        // Query Firestore to find the marker document by nameOfPlace
        val markerRef = db.collection("marker").whereEqualTo("nameOfPlace", markerData.nameOfPlace)

        markerRef.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                // Assuming nameOfPlace is unique in the collection, we get the first document
                val document = querySnapshot.documents[0]
                val marker = document.toObject(MarkerData::class.java)

                if (marker != null) {
                    // Create a new list with the updated or added rating
                    val updatedRatings = marker.ratings.toMutableList()

                    // Find if the rating already exists
                    val existingRatingIndex = updatedRatings.indexOfFirst { it.username == username }

                    if (existingRatingIndex != -1) {
                        // Rating already exists, update it
                        updatedRatings[existingRatingIndex] = updatedRatings[existingRatingIndex].copy(rating = newRating)
                    } else {
                        // Rating does not exist, add a new rating
                        updatedRatings.add(UserRating(username, newRating))
                    }

                    // Replace the old marker with a new one that has the updated ratings
                    val updatedMarker = marker.copy(ratings = updatedRatings.toList()) // Convert back to List<UserRating>

                    // Save the updated marker back to Firestore
                    document.reference.set(updatedMarker)
                        .addOnSuccessListener {
                            Log.d("MapFragment", "Rating updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.d("MapFragment", "Failed to update rating: ${e.message}")
                        }
                }
            } else {
                Log.d("MapFragment", "Marker not found for nameOfPlace: ${markerData.nameOfPlace}")
            }
        }.addOnFailureListener { e ->
            Log.d("MapFragment", "Failed to retrieve marker: ${e.message}")
        }
    }


    private fun loadReviews(markerData: MarkerData, adapter: DialogPostAdapter) {
        Log.d("MapFragment", "loadReviews")
        val db = FirebaseFirestore.getInstance()

        // Query the marker to get its notes (review IDs)
        db.collection("marker")
            .whereEqualTo("nameOfPlace", markerData.nameOfPlace)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val markerDoc = querySnapshot.documents.first()
                    val reviewIds = markerDoc["notes"] as? List<String> ?: emptyList()

                    // Fetch review details for each review ID
                    if (reviewIds.isNotEmpty()) {
                        db.collection("review")
                            .whereIn(FieldPath.documentId(), reviewIds)
                            .get()
                            .addOnSuccessListener { reviewSnapshot ->
                                // Filter out null values and ensure all items are valid ReviewModel objects
                                val reviews = reviewSnapshot.documents.mapNotNull { doc ->
                                    val review = doc.toObject(ReviewModel::class.java)
                                    review?.copy(id = doc.id)  // Add the document ID to the ReviewModel
                                }

                                // Update the RecyclerView adapter
                                adapter.updateData(reviews)
                            }
                            .addOnFailureListener { e ->
                                Log.e("MapFragment", "Failed to load reviews: ${e.message}")
                            }
                    } else {
                        Log.d("MapFragment", "No reviews found for this marker.")
                    }
                } else {
                    Log.e("MapFragment", "Marker not found for nameOfPlace: ${markerData.nameOfPlace}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("MapFragment", "Failed to query marker: ${e.message}")
            }
    }




    private fun handleAddReview(
        markerData: MarkerData,
        bottomSheetView: View,
        adapter: DialogPostAdapter
    ) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(requireContext(), "You need to log in to add a review", Toast.LENGTH_SHORT).show()
            return
        }

        val reviewText = bottomSheetView.findViewById<EditText>(R.id.editTextText).text.toString()

        if (reviewText.isBlank()) {
            Toast.makeText(requireContext(), "Please enter a review", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserEmail = currentUser.email ?: "Anonymous"
        getUsernameFromFirestore(currentUserEmail) { username ->
            if (username == null) {
                Toast.makeText(requireContext(), "Failed to retrieve username", Toast.LENGTH_SHORT).show()
                return@getUsernameFromFirestore
            }

            val db = FirebaseFirestore.getInstance()

            // Create a new review model with the retrieved username
            val review = ReviewModel(
                author = username,
                notes = reviewText,
                imageId = R.drawable.placeholder, // Placeholder image ID
                rating = 0 // Default rating
            )

            // Add the review to the Firestore "review" collection
            db.collection("review")
                .add(review)
                .addOnSuccessListener { reviewRef ->
                    val reviewWithId = review.copy(id = reviewRef.id)  // Update the review with the generated ID

                    // Now add the review ID to the marker's notes
                    db.collection("marker")
                        .whereEqualTo("nameOfPlace", markerData.nameOfPlace)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val markerDoc = querySnapshot.documents.first()
                                val markerRef = db.collection("marker").document(markerDoc.id)

                                markerRef.update("notes", FieldValue.arrayUnion(reviewWithId.id))
                                    .addOnSuccessListener {
                                        Log.d("MapFragment", "Review added successfully")
                                        loadReviews(markerData, adapter)
                                        bottomSheetView.findViewById<EditText>(R.id.editTextText).text.clear()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("MapFragment", "Failed to update marker's notes: ${e.message}")
                                    }
                            } else {
                                Log.e("MapFragment", "Marker not found for nameOfPlace: ${markerData.nameOfPlace}")
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("MapFragment", "Failed to add review: ${e.message}")
                }
        }
    }

    private fun filterSuggestions(query: String) {
        if (query.isNotEmpty()) {
            fetchMarkersFromFirebase { markers ->
                // Filter markers where nameOfPlace contains the query as a substring (case insensitive)
                val filteredMarkers = markers.filter {
                    it.nameOfPlace.startsWith(query, ignoreCase = true)
                }

                // Update the adapter with filtered data
                suggestionsAdapter.updateData(filteredMarkers)

                // Toggle the visibility of RecyclerView based on filtered results
                if (filteredMarkers.isEmpty()) {
                    recyclerView.visibility = View.GONE // Hide RecyclerView if no suggestions
                } else {
                    recyclerView.visibility = View.VISIBLE // Show RecyclerView if there are suggestions
                }
            }
        } else {
            // Optionally, handle empty query case (clear suggestions)
            fetchMarkersFromFirebase { markers ->
                suggestionsAdapter.updateData(emptyList()) // Empty list to hide suggestions
                recyclerView.visibility = View.GONE
            }
        }
    }

    private fun fetchMarkersFromFirebase(onMarkersFetched: (List<MarkerData>) -> Unit) {
        val database = FirebaseFirestore.getInstance()

        database.collection("marker")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val markerList = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(MarkerData::class.java)
                }
                onMarkersFetched(markerList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching markers: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getUsernameFromFirestore(currentUserEmail: String, onComplete: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("email", currentUserEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Assuming each email is unique and only one document will match
                    val document = querySnapshot.documents[0]
                    val username = document.getString("username")
                    onComplete(username) // Pass the retrieved username to the callback
                } else {
                    Log.d("MapFragment", "No user found with email: $currentUserEmail")
                    onComplete(null) // No user found
                }
            }
            .addOnFailureListener { e ->
                Log.d("MapFragment", "Failed to retrieve username: ${e.message}")
                onComplete(null) // Handle failure
            }
    }

}