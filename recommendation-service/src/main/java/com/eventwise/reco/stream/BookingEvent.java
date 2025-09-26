package com.eventwise.reco.stream;

public class BookingEvent {
  private String type;
  private Long userId;
  private Long eventId;
  private Integer qty;

  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Long getEventId() { return eventId; }
  public void setEventId(Long eventId) { this.eventId = eventId; }
  public Integer getQty() { return qty; }
  public void setQty(Integer qty) { this.qty = qty; }
}
