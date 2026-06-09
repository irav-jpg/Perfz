package com.example.perfz.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.perfz.R
import com.example.perfz.MainActivity
import com.example.perfz.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null) {
            val uid = firebaseUser.uid

            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (_binding == null) return@addOnSuccessListener

                    if (document != null && document.exists()) {
                        val nombre = document.getString("nombre") ?: "Usuario"
                        val apellidos = document.getString("apellidos") ?: "No registrado"
                        val correo = document.getString("correo") ?: (firebaseUser.email ?: "Sin correo")
                        val contrasena = document.getString("contraseña") ?: ""
                        val telefono = document.getString("telefono") ?: "No registrado"
                        val fechaNacimiento = document.getString("fechaNacimiento") ?: "No registrada"


                        binding.tvProfileName.text = nombre
                        binding.tvProfileFirstName.text = nombre
                        binding.tvProfileLastName.text = apellidos
                        binding.tvProfileEmail.text = correo
                        binding.tvProfilePhone.text = telefono
                        binding.tvProfileBirthdate.text = fechaNacimiento


                        binding.etProfilePassword.setText(contrasena)


                        val primeraLetra = nombre.trim().firstOrNull()?.toString()?.uppercase() ?: "U"
                        binding.tvProfileLetter.text = primeraLetra
                    } else {
                        val emailFallback = firebaseUser.email ?: "usuario@perfz.com"
                        val nameFallback = emailFallback.substringBefore("@").replaceFirstChar { it.uppercase() }
                        binding.tvProfileName.text = nameFallback
                        binding.tvProfileFirstName.text = nameFallback
                        binding.tvProfileEmail.text = emailFallback
                        binding.tvProfileLetter.text = nameFallback.trim().firstOrNull()?.toString()?.uppercase() ?: "U"
                        binding.etProfilePassword.setText("")
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error al obtener perfil: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }


        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}