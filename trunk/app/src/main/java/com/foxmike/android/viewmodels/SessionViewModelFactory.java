package com.foxmike.android.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by chris on 2019-03-22.
 */

public class SessionViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private String data1;

    public SessionViewModelFactory(String data1) {
        this.data1 = data1;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (SessionViewModel.class.isAssignableFrom(modelClass)) {
            try {
                return modelClass.getConstructor(SessionViewModel.class).newInstance(data1);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return super.create(modelClass);
    }

}