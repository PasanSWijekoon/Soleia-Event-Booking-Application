package com.strawhats.soleia.Models;


import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.strawhats.soleia.R;

public class CustomToast {

    private Context context;

    public CustomToast(Context context) {
        this.context = context;
    }

    // Accept raw resource ID for Lottie animation
    public void showToast(String message, int rawResId) {
        // Create the Toast object
        Toast toast = new Toast(context);

        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.customtoast, null);

        // Find and set up the LottieAnimationView
        LottieAnimationView lottieAnimationView = layout.findViewById(R.id.lottie_animation_view);
        lottieAnimationView.setAnimation(rawResId);  // Set the Lottie animation using raw resource ID
        //lottieAnimationView.setLoop(true);  // Loop the animation
        lottieAnimationView.playAnimation();  // Start playing the animation

        // Find and set up the TextView
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        // Configure the toast
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG); // Longer duration for animation visibility
        toast.setView(layout);

        // Show the toast
        toast.show();
    }
}
