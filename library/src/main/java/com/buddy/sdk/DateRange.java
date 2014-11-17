package com.buddy.sdk;
import java.util.Date;


public class DateRange {
   
   Date start;
   Date end;

   public DateRange(Date start, Date end) {
   		this.start = start;
   		this.end = end;
   }

   public Date getStart() {
   		if (this.start == null) {
   			return new Date(0);
   		}
   		return this.start;
   }

   public Date getEnd() {
   		if (this.end == null) {
   			return new Date();
   		}
   		return this.end;
   }
}
