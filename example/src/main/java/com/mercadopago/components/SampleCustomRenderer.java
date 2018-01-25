package com.mercadopago.plugins.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.mercadopago.components.Renderer;
import com.mercadopago.components.SampleCustomComponent;
import com.mercadopago.examples.R;

/**
 * Created by nfortuna on 1/24/18.
 */
public class SampleCustomRenderer extends Renderer<SampleCustomComponent> {
    @Override
    public View render(final SampleCustomComponent component, final Context context) {
        return LayoutInflater.from(context)
            .inflate(R.layout.mpsdk_sample_custom_component, null);
    }
}