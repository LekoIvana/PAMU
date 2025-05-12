package ba.sum.fpmoz.pamu

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordToggle: ImageButton
    private lateinit var emailStatusIcon: ImageView
    private lateinit var registerButton: Button
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var userName: EditText
    private lateinit var confirmPasswordToggle: ImageButton
    private lateinit var googleIcon: ImageView

    private var confirmPasswordVisible = false
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        
        userName = findViewById(R.id.userName)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        passwordToggle = findViewById(R.id.passwordToggle)
        emailStatusIcon = findViewById(R.id.emailStatusIcon)
        registerButton = findViewById(R.id.registerButton)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        confirmPasswordToggle = findViewById(R.id.confirmPasswordToggle)
        googleIcon = findViewById(R.id.googleIcon)

        
        googleIcon.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }

        
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailStatusIcon.setImageResource(R.drawable.ic_check)
                    emailStatusIcon.visibility = View.VISIBLE
                } else {
                    emailStatusIcon.setImageResource(R.drawable.ic_user)
                    emailStatusIcon.visibility = if (email.isNotEmpty()) View.VISIBLE else View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        
        passwordToggle.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.setImageResource(R.drawable.ic_visibility)
            } else {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.setImageResource(R.drawable.ic_visibility_off)
            }
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        confirmPasswordToggle.setOnClickListener {
            confirmPasswordVisible = !confirmPasswordVisible
            if (confirmPasswordVisible) {
                confirmPasswordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                confirmPasswordToggle.setImageResource(R.drawable.ic_visibility)
            } else {
                confirmPasswordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                confirmPasswordToggle.setImageResource(R.drawable.ic_visibility_off)
            }
            confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)
        }

        val goToLogin = findViewById<TextView>(R.id.goToLogin)
        goToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val name = userName.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()

                        val db = Firebase.firestore
                        val uid = auth.currentUser?.uid
                        val user = hashMapOf(
                            "userName" to name,
                            "email" to email
                        )

                        if (uid != null) {
                            db.collection("users").document(uid).set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registration complete!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to save user: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google prijava neuspješna: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Firebase prijava neuspješna.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
