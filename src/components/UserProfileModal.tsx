import React, { useState, useEffect } from 'react';
import { X, User, Mail, Phone, MapPin, Upload, FileText, Camera, Save, Eye } from 'lucide-react';

interface UserProfile {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  enabled: boolean;
  alternateEmail?: string;
  phoneNumber?: string;
  alternatePhoneNumber?: string;
  address1Line1?: string;
  address1Line2?: string;
  address1City?: string;
  address1State?: string;
  address1PostalCode?: string;
  address1Country?: string;
  address2Line1?: string;
  address2Line2?: string;
  address2City?: string;
  address2State?: string;
  address2PostalCode?: string;
  address2Country?: string;
  profilePictureUrl?: string;
  idDocument1Url?: string;
  idDocument1Filename?: string;
  idDocument2Url?: string;
  idDocument2Filename?: string;
  createdAt: string;
  updatedAt: string;
}

interface UserProfileModalProps {
  isOpen: boolean;
  onClose: () => void;
  userId: number;
  token: string;
  isOwner: boolean; // true if current user is owner (can edit), false if viewing own profile
}

const UserProfileModal: React.FC<UserProfileModalProps> = ({ 
  isOpen, 
  onClose, 
  userId, 
  token, 
  isOwner 
}) => {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState<'basic' | 'contact' | 'address' | 'documents'>('basic');

  useEffect(() => {
    if (isOpen && userId) {
      fetchUserProfile();
    }
  }, [isOpen, userId]);

  const fetchUserProfile = async () => {
    try {
      setLoading(true);
      const endpoint = isOwner ? `/api/owner/users/${userId}` : '/api/user/profile';
      
      const response = await fetch(endpoint, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setProfile(data);
      } else {
        setError('Failed to load user profile');
      }
    } catch (err) {
      setError('Network error');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (!profile || !isOwner) return;

    try {
      setSaving(true);
      setError('');

      const response = await fetch(`/api/owner/users/${userId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(profile),
      });

      if (response.ok) {
        const updatedProfile = await response.json();
        setProfile(updatedProfile);
        alert('Profile updated successfully!');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Failed to update profile');
      }
    } catch (err) {
      setError('Network error');
    } finally {
      setSaving(false);
    }
  };

  const handleFileUpload = async (file: File, type: 'profile' | 'document1' | 'document2') => {
    if (!isOwner) return;

    try {
      const formData = new FormData();
      formData.append('file', file);

      let endpoint = '';
      switch (type) {
        case 'profile':
          endpoint = `/api/owner/users/${userId}/profile-picture`;
          break;
        case 'document1':
          endpoint = `/api/owner/users/${userId}/id-document1`;
          break;
        case 'document2':
          endpoint = `/api/owner/users/${userId}/id-document2`;
          break;
      }

      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
        body: formData,
      });

      if (response.ok) {
        const result = await response.json();
        
        // Update profile state
        if (profile) {
          const updatedProfile = { ...profile };
          switch (type) {
            case 'profile':
              updatedProfile.profilePictureUrl = result.imageUrl;
              break;
            case 'document1':
              updatedProfile.idDocument1Url = result.documentUrl;
              updatedProfile.idDocument1Filename = result.filename;
              break;
            case 'document2':
              updatedProfile.idDocument2Url = result.documentUrl;
              updatedProfile.idDocument2Filename = result.filename;
              break;
          }
          setProfile(updatedProfile);
        }
        
        alert(`${type === 'profile' ? 'Profile picture' : 'Document'} uploaded successfully!`);
      } else {
        const errorData = await response.json();
        alert(errorData.message || 'Upload failed');
      }
    } catch (err) {
      alert('Upload failed');
    }
  };

  const handleInputChange = (field: keyof UserProfile, value: string) => {
    if (!profile || !isOwner) return;
    
    setProfile({
      ...profile,
      [field]: value
    });
  };

  const countries = [
    'Afghanistan', 'Albania', 'Algeria', 'Argentina', 'Australia', 'Austria', 'Bahrain', 'Bangladesh', 
    'Belgium', 'Brazil', 'Canada', 'China', 'Denmark', 'Egypt', 'France', 'Germany', 'India', 
    'Indonesia', 'Iran', 'Iraq', 'Italy', 'Japan', 'Jordan', 'Kuwait', 'Lebanon', 'Malaysia', 
    'Netherlands', 'Norway', 'Oman', 'Pakistan', 'Philippines', 'Qatar', 'Russia', 'Saudi Arabia', 
    'Singapore', 'South Korea', 'Spain', 'Sweden', 'Switzerland', 'Thailand', 'Turkey', 'UAE', 
    'United Kingdom', 'United States', 'Yemen'
  ];

  if (!isOpen) return null;

  if (loading) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-xl p-8">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="text-center mt-4 text-gray-600">Loading profile...</p>
        </div>
      </div>
    );
  }

  if (error || !profile) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-xl p-8 max-w-md w-full mx-4">
          <div className="text-center">
            <div className="w-16 h-16 mx-auto mb-4 bg-red-100 rounded-full flex items-center justify-center">
              <X className="h-8 w-8 text-red-600" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Error</h3>
            <p className="text-gray-600 mb-4">{error}</p>
            <button
              onClick={onClose}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200 sticky top-0 bg-white z-10">
          <div className="flex items-center space-x-3">
            <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
              {profile.profilePictureUrl ? (
                <img
                  src={profile.profilePictureUrl}
                  alt="Profile"
                  className="w-12 h-12 rounded-full object-cover"
                />
              ) : (
                <User className="h-6 w-6 text-blue-600" />
              )}
            </div>
            <div>
              <h2 className="text-2xl font-bold text-gray-900">
                {isOwner ? 'Edit User Profile' : 'My Profile'}
              </h2>
              <p className="text-gray-600">{profile.firstName} {profile.lastName}</p>
            </div>
          </div>
          <div className="flex items-center space-x-2">
            {isOwner && (
              <button
                onClick={handleSave}
                disabled={saving}
                className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 flex items-center space-x-2"
              >
                <Save className="h-4 w-4" />
                <span>{saving ? 'Saving...' : 'Save Changes'}</span>
              </button>
            )}
            <button
              onClick={onClose}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <X className="h-6 w-6 text-gray-500" />
            </button>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="border-b border-gray-200">
          <nav className="flex space-x-8 px-6">
            {[
              { id: 'basic', label: 'Basic Info', icon: User },
              { id: 'contact', label: 'Contact', icon: Mail },
              { id: 'address', label: 'Addresses', icon: MapPin },
              { id: 'documents', label: 'Documents', icon: FileText }
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors flex items-center space-x-2 ${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <tab.icon className="h-4 w-4" />
                <span>{tab.label}</span>
              </button>
            ))}
          </nav>
        </div>

        {/* Tab Content */}
        <div className="p-6">
          {/* Basic Info Tab */}
          {activeTab === 'basic' && (
            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    First Name
                  </label>
                  <input
                    type="text"
                    value={profile.firstName}
                    onChange={(e) => handleInputChange('firstName', e.target.value)}
                    disabled={!isOwner}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Last Name
                  </label>
                  <input
                    type="text"
                    value={profile.lastName}
                    onChange={(e) => handleInputChange('lastName', e.target.value)}
                    disabled={!isOwner}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Email Address
                  </label>
                  <input
                    type="email"
                    value={profile.email}
                    disabled
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50"
                  />
                  <p className="text-xs text-gray-500 mt-1">Email cannot be changed</p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Role
                  </label>
                  <select
                    value={profile.role}
                    onChange={(e) => handleInputChange('role', e.target.value)}
                    disabled={!isOwner}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                  >
                    <option value="USER">User</option>
                    <option value="ADMIN">Admin</option>
                    <option value="OWNER">Owner</option>
                  </select>
                </div>
              </div>

              {/* Profile Picture */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Profile Picture
                </label>
                <div className="flex items-center space-x-4">
                  <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center overflow-hidden">
                    {profile.profilePictureUrl ? (
                      <img
                        src={profile.profilePictureUrl}
                        alt="Profile"
                        className="w-20 h-20 object-cover"
                      />
                    ) : (
                      <Camera className="h-8 w-8 text-gray-400" />
                    )}
                  </div>
                  {isOwner && (
                    <div>
                      <input
                        type="file"
                        accept="image/*"
                        onChange={(e) => {
                          const file = e.target.files?.[0];
                          if (file) handleFileUpload(file, 'profile');
                        }}
                        className="hidden"
                        id="profile-picture-upload"
                      />
                      <label
                        htmlFor="profile-picture-upload"
                        className="cursor-pointer bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
                      >
                        <Upload className="h-4 w-4" />
                        <span>Upload Picture</span>
                      </label>
                      <p className="text-xs text-gray-500 mt-1">Max 5MB, JPG/PNG only</p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Contact Tab */}
          {activeTab === 'contact' && (
            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Alternate Email
                  </label>
                  <input
                    type="email"
                    value={profile.alternateEmail || ''}
                    onChange={(e) => handleInputChange('alternateEmail', e.target.value)}
                    disabled={!isOwner}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                    placeholder="alternate@example.com"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Phone Number (with country code)
                  </label>
                  <input
                    type="tel"
                    value={profile.phoneNumber || ''}
                    onChange={(e) => handleInputChange('phoneNumber', e.target.value)}
                    disabled={!isOwner}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                    placeholder="+973 1234 5678"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Alternate Phone Number (with country code)
                  </label>
                  <input
                    type="tel"
                    value={profile.alternatePhoneNumber || ''}
                    onChange={(e) => handleInputChange('alternatePhoneNumber', e.target.value)}
                    disabled={!isOwner}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                    placeholder="+973 9876 5432"
                  />
                </div>
              </div>
            </div>
          )}

          {/* Address Tab */}
          {activeTab === 'address' && (
            <div className="space-y-8">
              {/* Address 1 */}
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-4">Primary Address</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Address Line 1
                    </label>
                    <input
                      type="text"
                      value={profile.address1Line1 || ''}
                      onChange={(e) => handleInputChange('address1Line1', e.target.value)}
                      disabled={!isOwner}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                      placeholder="Street address"
                    />
                  </div>

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Address Line 2
                    </label>
                    <input
                      type="text"
                      value={profile.address1Line2 || ''}
                      onChange={(e) => handleInputChange('address1Line2', e.target.value)}
                      disabled={!isOwner}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                      placeholder="Apartment, suite, etc. (optional)"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      City
                    </label>
                    <input
                      type="text"
                      value={profile.address1City || ''}
                      onChange={(e) => handleInputChange('address1City', e.target.value)}
                      disabled={!isOwner}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      State/Province
                    </label>
                    <input
                      type="text"
                      value={profile.address1State || ''}
                      onChange={(e) => handleInputChange('address1State', e.target.value)}
                      disabled={!isOwner}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Postal Code
                    </label>
                    <input
                      type="text"
                      value={profile.address1PostalCode || ''}
                      onChange={(e) => handleInputChange('address1PostalCode', e.target.value)}
                      disabled={!isOwner}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Country
                    </label>
                    <select
                      value={profile.address1Country || ''}
                      onChange={(e) => handleInputChange('address1Country', e.target.value)}
                      disabled={!isOwner}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                    >
                      <option value="">Select Country</option>
                      {countries.map(country => (
                        <option key={country} value={country}>{country}</option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>

              {/* Address 2 */}
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-4">Secondary Address (Optional)</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Address Line 1
                    </label>
                    <input
                      type="text"
                      value={profile.address2Line1 || ''}
                      onChange={(e) => handleInputChange('address2Line1', e.target.value)}
                      disabled={!isOwner}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                      placeholder="Street address"
                    />
                  </div>

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Address Line 2
                    </label>
                    <input
                      type="text"
                      value={profile.address2Line2 || ''}
                      onChange={(e) => handleInputChange('address2Line2', e.target.value)}
                      disabled={!isOwner}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                      placeholder="Apartment, suite, etc. (optional)"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      City
                    </label>
                    <input
                      type="text"
                      value={profile.address2City || ''}
                      onChange={(e) => handleInputChange('address2City', e.target.value)}
                      disabled={!isOwner}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      State/Province
                    </label>
                    <input
                      type="text"
                      value={profile.address2State || ''}
                      onChange={(e) => handleInputChange('address2State', e.target.value)}
                      disabled={!isOwner}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Postal Code
                    </label>
                    <input
                      type="text"
                      value={profile.address2PostalCode || ''}
                      onChange={(e) => handleInputChange('address2PostalCode', e.target.value)}
                      disabled={!isOwner}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Country
                    </label>
                    <select
                      value={profile.address2Country || ''}
                      onChange={(e) => handleInputChange('address2Country', e.target.value)}
                      disabled={!isOwner}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50"
                    >
                      <option value="">Select Country</option>
                      {countries.map(country => (
                        <option key={country} value={country}>{country}</option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Documents Tab */}
          {activeTab === 'documents' && (
            <div className="space-y-6">
              {/* ID Document 1 */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  ID Document 1
                </label>
                <div className="flex items-center space-x-4">
                  <div className="flex-1">
                    {profile.idDocument1Url ? (
                      <div className="flex items-center space-x-2 p-3 bg-gray-50 rounded-lg">
                        <FileText className="h-5 w-5 text-gray-500" />
                        <span className="text-sm text-gray-700">{profile.idDocument1Filename}</span>
                        <a
                          href={profile.idDocument1Url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-blue-600 hover:text-blue-700"
                        >
                          <Eye className="h-4 w-4" />
                        </a>
                      </div>
                    ) : (
                      <div className="p-3 bg-gray-50 rounded-lg text-center text-gray-500">
                        No document uploaded
                      </div>
                    )}
                  </div>
                  {isOwner && (
                    <div>
                      <input
                        type="file"
                        accept=".pdf,.jpg,.jpeg,.png"
                        onChange={(e) => {
                          const file = e.target.files?.[0];
                          if (file) handleFileUpload(file, 'document1');
                        }}
                        className="hidden"
                        id="document1-upload"
                      />
                      <label
                        htmlFor="document1-upload"
                        className="cursor-pointer bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
                      >
                        <Upload className="h-4 w-4" />
                        <span>Upload</span>
                      </label>
                    </div>
                  )}
                </div>
                <p className="text-xs text-gray-500 mt-1">Max 10MB, PDF/JPG/PNG only</p>
              </div>

              {/* ID Document 2 */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  ID Document 2
                </label>
                <div className="flex items-center space-x-4">
                  <div className="flex-1">
                    {profile.idDocument2Url ? (
                      <div className="flex items-center space-x-2 p-3 bg-gray-50 rounded-lg">
                        <FileText className="h-5 w-5 text-gray-500" />
                        <span className="text-sm text-gray-700">{profile.idDocument2Filename}</span>
                        <a
                          href={profile.idDocument2Url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-blue-600 hover:text-blue-700"
                        >
                          <Eye className="h-4 w-4" />
                        </a>
                      </div>
                    ) : (
                      <div className="p-3 bg-gray-50 rounded-lg text-center text-gray-500">
                        No document uploaded
                      </div>
                    )}
                  </div>
                  {isOwner && (
                    <div>
                      <input
                        type="file"
                        accept=".pdf,.jpg,.jpeg,.png"
                        onChange={(e) => {
                          const file = e.target.files?.[0];
                          if (file) handleFileUpload(file, 'document2');
                        }}
                        className="hidden"
                        id="document2-upload"
                      />
                      <label
                        htmlFor="document2-upload"
                        className="cursor-pointer bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
                      >
                        <Upload className="h-4 w-4" />
                        <span>Upload</span>
                      </label>
                    </div>
                  )}
                </div>
                <p className="text-xs text-gray-500 mt-1">Max 10MB, PDF/JPG/PNG only</p>
              </div>

              {!isOwner && (
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <p className="text-sm text-blue-700">
                    <strong>Note:</strong> Only the system owner can modify profile information and upload documents. 
                    If you need to update your profile, please contact your system administrator.
                  </p>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default UserProfileModal;