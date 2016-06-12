package com.nfc.project.vo;

import org.json.simple.JSONObject;

public class UserVO {

		private String id;
		private String pw;
		private String name;
		private String type;
		
		
		
		
		public UserVO() {
			super();
		}
		public UserVO(String id, String pw, String name, String type) {
			super();
			this.id = id;
			this.pw = pw;
			this.name = name;
			this.type = type;
		}
		
		
		
		public UserVO(String id, String name) {
			super();
			this.id = id;
			this.name = name;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getPw() {
			return pw;
		}
		public void setPw(String pw) {
			this.pw = pw;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		
		
		@Override
		public String toString() {
			return "UserVO [id=" + id + ", pw=" + pw + ", name=" + name + ", type=" + type + "]";
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((pw == null) ? 0 : pw.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UserVO other = (UserVO) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (pw == null) {
				if (other.pw != null)
					return false;
			} else if (!pw.equals(other.pw))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}
		
}
