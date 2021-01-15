/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openfoodfacts.github.scrachx.openfood.features

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivitySignupBinding
import openfoodfacts.github.scrachx.openfood.utils.Utils

/**
 * A sign-up screen that offers sign-up via email/name/username/password.
 */

class SignUpActivity : AppCompatActivity() {
    private var _binding: ActivitySignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        _binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=getString(R.string.signup)
        binding.btnSignup.setOnClickListener{ doAttemptSignUp()}
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
    }

    private fun doAttemptSignUp(){
        Utils.hideKeyboard(this)

        val name=binding.signupName.text.toString()
        val email=binding.signupInput.text.toString()
        val username=binding.signupUserName.text.toString()
        val password=binding.passInput.text.toString()
        val confirmPassword=binding.passConfirmInput.text.toString()

        if(name.isBlank()){
            binding.signupName.error=getString(R.string.error_field_required)
            binding.signupName.requestFocus()
            return
        }
        if(email.isBlank()){
            binding.signupInput.error=getString(R.string.error_field_required)
            binding.signupInput.requestFocus()
            return
        }
        if(username.isBlank()){
            binding.signupUserName.error=getString(R.string.error_field_required)
            binding.signupUserName.requestFocus()
            return
        }
        if(password.isBlank()){
            binding.passInput.error=getString(R.string.error_field_required)
            binding.passInput.requestFocus()
            return
        }
        if (password.length < 6) {
            binding.passInput.error = getText(R.string.error_invalid_password)
            binding.passInput.requestFocus()
            return
        }
        if(confirmPassword.isBlank()){
            binding.passConfirmInput.error=getString(R.string.error_field_required)
            binding.passConfirmInput.requestFocus()
            return
        }
        if(password != confirmPassword){
            binding.passConfirmInput.error = getText(R.string.confirm_password_error)
            binding.passConfirmInput.requestFocus()
            return
        }
        // End checks
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}