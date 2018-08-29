package com.team.eddie.uber_alles.ui.login

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.team.eddie.uber_alles.R
import com.team.eddie.uber_alles.ui.map.CustomerMapActivity
import com.team.eddie.uber_alles.ui.map.DriverMapActivity
import com.team.eddie.uber_alles.utils.SaveSharedPreference
import com.team.eddie.uber_alles.utils.isEmailValid
import com.team.eddie.uber_alles.utils.isPasswordValid
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAuthListener: FirebaseAuth.AuthStateListener

    private val USER: String = "Users";
    private val ALL_USER: String = "All_Users";
    private val DRIVER: String = "Drivers";
    private val CUSTOMER: String = "Customers";

    private val IS_DRIVER: String = "is_driver";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        firebaseAuthListener = FirebaseAuth.AuthStateListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) onLoginSuccess()
        }

        logInButton.setOnClickListener { attemptLogin() }
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(firebaseAuthListener)
    }

    private fun onLoginSuccess() {

        val userId = mAuth.currentUser!!.uid
        FirebaseDatabase.getInstance().reference.child(ALL_USER).child(userId).child(IS_DRIVER)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var isDriver: Boolean = dataSnapshot.getValue().toString().toBoolean()

                        SaveSharedPreference.setLoggedIn(applicationContext, emailTextInputEdit.text.toString())
                        SaveSharedPreference.setUserType(applicationContext, isDriver)

                        if (isDriver)
                            startActivity(DriverMapActivity.getLaunchIntent(this@LoginActivity))
                        else
                            startActivity(CustomerMapActivity.getLaunchIntent(this@LoginActivity))
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
    }

    private fun attemptLogin() {

        if (localLogin()) {
            val email = emailTextInputEdit.text.toString()
            val password = passwordTextInputEdit.text.toString()

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (!task.isSuccessful)
                    Toast.makeText(this, "Couldn't Authenticate", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun localLogin(): Boolean {

        var success = true
        var failedFocusView: View? = null

        emailTextInput.error = null
        passwordTextInput.error = null

        if (!checkPassword(passwordTextInputEdit.text.toString())) {
            failedFocusView = passwordTextInput
            success = false
        }

        if (!checkEmail(emailTextInputEdit.text.toString())) {
            failedFocusView = emailTextInput
            success = false
        }

        if (!success) failedFocusView?.requestFocus()

        return success
    }

    private fun checkEmail(email: String): Boolean {
        if (TextUtils.isEmpty(email)) {
            emailTextInput.error = getString(R.string.error_field_required)
            return false
        } else if (!isEmailValid(email)) {
            emailTextInput.error = getString(R.string.error_invalid_email)
            return false
        }

        return true
    }

    private fun checkPassword(password: String): Boolean {
        if (TextUtils.isEmpty(password)) {
            passwordTextInput.error = getString(R.string.error_field_required)
            return false
        } else if (!isPasswordValid(password)) {
            passwordTextInput.error = getString(R.string.error_invalid_password)
            return false
        }

        return true
    }

}
