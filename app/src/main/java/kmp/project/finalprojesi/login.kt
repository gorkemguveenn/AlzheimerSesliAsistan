package kmp.project.finalprojesi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun login(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Blue, Color.Black),
                    startY = 0.3f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, top = 40.dp)
            ) {
                Text(
                    text = "Giriş Yap",
                    style = TextStyle(
                        fontSize = 34.sp,
                        fontWeight = FontWeight(700),
                        color = Color(0xFFF9FAFB),
                    )
                )
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                logincard(navController)
            }
        }
    }
}

@Composable
fun logincard(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val context = LocalContext.current
    val dbHelper = remember { UserDatabaseHelper(context) }

    Column(
        modifier = Modifier
            .shadow(
                elevation = 28.dp,
                spotColor = Color(0x142D368A),
                ambientColor = Color(0x142D368A)
            )
            .width(335.dp)
            .height(300.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Kullanıcı Adı") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                // Kullanıcının girdiği email ve şifreyi veritabanında kontrol et
                if (dbHelper.isValidUser(email, password)) {
                    message = "Giriş başarılı!"
                    navController.navigate("greeting") // Greetle sayfasına yönlendirme
                } else {
                    message = "Kullanıcı adı veya şifre hatalı!"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Login")
        }
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clickable { navController.navigate("sign") }, // kayıt ol sayfasına yönlendirme
                text = "Kayıt ol",
                style = TextStyle(
                    color = Color.Blue,              // Mavi renk
                    fontSize = 18.sp,                // 18sp yazı boyutu
                    textDecoration = TextDecoration.Underline // Altı çizili
                ),
            )

        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, color = Color.Red)
    }
}
