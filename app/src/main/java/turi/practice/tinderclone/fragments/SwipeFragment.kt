package turi.practice.tinderclone.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import kotlinx.android.synthetic.main.fragment_swipe.*

import turi.practice.tinderclone.R
import turi.practice.tinderclone.activities.TinderCallBack
import turi.practice.tinderclone.adapters.CardsAdapter
import turi.practice.tinderclone.util.*

class SwipeFragment : Fragment() {
    private var callback: TinderCallBack? = null
    private lateinit var userId: String
    private lateinit var userDatabase: DatabaseReference
    private lateinit var chatDatabase: DatabaseReference
    private var cardsAdapter: ArrayAdapter<Users>? = null
    private var rowItems = ArrayList<Users>()
    private var preferredGender: String? = null
    private var userName: String? = null
    private var imageUrl: String? = null

    fun setCallBack(callback: TinderCallBack) {
        this.callback = callback
        userId = callback.onGetUserId()
        userDatabase = callback.getUserDatabase()
        chatDatabase = callback.getChatDatabase()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_swipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userDatabase.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(Users::class.java)
                preferredGender = user?.preferredGender
                userName = user?.name
                imageUrl = user?.imageUrl
                populateItems()
            }
        })
        cardsAdapter = CardsAdapter(context!!, R.layout.item, rowItems)
        frame.adapter = cardsAdapter
        frame.setFlingListener(object : SwipeFlingAdapterView.onFlingListener {
            override fun removeFirstObjectInAdapter() {
                rowItems.removeAt(0)
                cardsAdapter?.notifyDataSetChanged()
            }

            override fun onLeftCardExit(p0: Any?) {
                var user = p0 as Users
                userDatabase.child(user.uid.toString()).child(DATA_SWIPES_LEFT).child(userId)
                    .setValue(true)
            }

            override fun onRightCardExit(p0: Any?) {
                val selectedUser = p0 as Users
                val selectedUserId = selectedUser.uid
                if (!selectedUserId.isNullOrEmpty()) {
                    userDatabase.child(userId).child(DATA_SWIPES_RIGHT)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {

                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                if (p0.hasChild(selectedUserId)) {
                                    Toast.makeText(context, "Match!", Toast.LENGTH_LONG).show()
                                    //Generate key for chatDB
                                    val chatKey = chatDatabase.push().key
                                    if (chatKey != null) {
                                        userDatabase.child(userId).child(DATA_SWIPES_RIGHT)
                                            .child(selectedUserId).removeValue()
                                        userDatabase.child(userId).child(DATA_MATCHES)
                                            .child(selectedUserId).setValue(chatKey)
                                        userDatabase.child(selectedUserId).child(DATA_MATCHES)
                                            .child(userId).setValue(chatKey)
                                        chatDatabase.child(chatKey).child(userId).child(DATA_NAME)
                                            .setValue(userName)
                                        chatDatabase.child(chatKey).child(userId)
                                            .child(DATA_IMAGE_URL)
                                            .setValue(imageUrl)
                                        chatDatabase.child(chatKey).child(selectedUserId)
                                            .child(DATA_NAME)
                                            .setValue(selectedUser.name)
                                        chatDatabase.child(chatKey).child(selectedUserId)
                                            .child(DATA_IMAGE_URL)
                                            .setValue(selectedUser.imageUrl)
                                    }
                                } else {
                                    userDatabase.child(selectedUserId).child(DATA_SWIPES_RIGHT)
                                        .child(userId).setValue(true)
                                }
                            }
                        })
                }
            }

            override fun onAdapterAboutToEmpty(p0: Int) {
            }

            override fun onScroll(p0: Float) {
            }
        })

        frame.setOnItemClickListener { position, data -> }
        likeButton.setOnClickListener {
            if (!rowItems.isEmpty()) {
                frame.topCardListener.selectRight()
            }
        }

        dislikeButton.setOnClickListener {
            if (!rowItems.isEmpty()) {
                frame.topCardListener.selectLeft()
            }
        }
    }

    fun populateItems() {
        noUsersLayout.visibility = View.GONE
        progressLayout.visibility = View.VISIBLE
        val cardQuery = userDatabase.orderByChild(DATA_GENDER).equalTo(preferredGender)
        cardQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach { child ->
                    val user = child.getValue(Users::class.java)
                    if (user != null) {
                        var showUser = true
                        if (child.child(DATA_SWIPES_LEFT).hasChild(userId) ||
                            child.child(DATA_SWIPES_RIGHT).hasChild(userId) ||
                            child.child(DATA_MATCHES).hasChild(userId)
                        ) {
                            showUser = false
                        }
                        if (showUser) {
                            rowItems.add(user)
                            cardsAdapter?.notifyDataSetChanged()
                        }
                    }
                }
                progressLayout.visibility = View.GONE
                if (rowItems.isEmpty()) {
                    noUsersLayout.visibility = View.VISIBLE
                }
            }
        })
    }
}
