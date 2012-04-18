package net.shrine.webclient.client.util;

public interface IObservable {
  void observedBy(final Observer observer);
  
  void noLongerObservedBy(final Observer observer);
  
  void notifyObservers();
}
