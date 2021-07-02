package com.namibox.tools;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;
import java.io.InputStream;

/**
 * A model loader for handling Android resource files. Model must be an InputStream.
 *
 */
public class StreamResourceLoader implements ModelLoader<InputStream, InputStream> {

  public StreamResourceLoader() {
  }

  @Override
  public LoadData<InputStream> buildLoadData(InputStream model, int width, int height, Options options) {
    return new LoadData<>(new ObjectKey(model), new DrawableDataFetcher(model));
  }

  @Override
  public boolean handles(InputStream model) {
    // TODO: check that this is in fact a resource id.
    return true;
  }

  public static class StreamFactory implements ModelLoaderFactory<InputStream, InputStream> {


    public StreamFactory() {
    }

    @Override
    public ModelLoader<InputStream, InputStream> build(MultiModelLoaderFactory multiFactory) {
      return new StreamResourceLoader();
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }


  public static class DrawableDataFetcher implements DataFetcher<InputStream> {
    private InputStream data;

    public DrawableDataFetcher(InputStream data) {
      this.data = data;
    }

    @Override
    public void loadData(Priority priority, DataCallback<? super InputStream> callback) {
      callback.onDataReady(data);
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {
      // Do nothing.
    }

    @Override
    public Class<InputStream> getDataClass() {
      return InputStream.class;
    }

    @Override
    public DataSource getDataSource() {
      return DataSource.LOCAL;
    }
  }
}
