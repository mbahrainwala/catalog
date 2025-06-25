import React from 'react';
import { Award, Users, Globe, Wrench, Shield, Clock, Target, Heart } from 'lucide-react';

const AboutUs: React.FC = () => {
  const stats = [
    { label: 'Years of Experience', value: '25+', icon: Clock },
    { label: 'Happy Clients', value: '500+', icon: Users },
    { label: 'Projects Completed', value: '1000+', icon: Target },
    { label: 'Countries Served', value: '15+', icon: Globe }
  ];

  const values = [
    {
      icon: Shield,
      title: 'Quality Assurance',
      description: 'We ensure all our equipment meets the highest international standards and safety requirements.'
    },
    {
      icon: Heart,
      title: 'Customer First',
      description: 'Our customers are at the heart of everything we do. We build lasting relationships based on trust.'
    },
    {
      icon: Wrench,
      title: 'Technical Excellence',
      description: 'Our team of experts provides comprehensive technical support and maintenance services.'
    },
    {
      icon: Award,
      title: 'Industry Leadership',
      description: 'We stay ahead of industry trends and continuously innovate to serve our clients better.'
    }
  ];

  return (
    <div className="space-y-12">
      {/* Header */}
      <div className="text-center">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">About Industrial Catalog</h1>
        <p className="text-xl text-gray-600 max-w-3xl mx-auto">
          Leading provider of drilling and mining equipment in the Kingdom of Bahrain and the Gulf region, 
          serving the industrial sector with excellence since 1999.
        </p>
      </div>

      {/* Company Story */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-center">
          <div>
            <h2 className="text-3xl font-bold text-gray-900 mb-6">Our Story</h2>
            <div className="space-y-4 text-gray-600 leading-relaxed">
              <p>
                Founded in 1999, Industrial Catalog began as a small family business with a vision to provide 
                high-quality drilling and mining equipment to the growing industrial sector in Bahrain. 
                What started as a modest operation has grown into one of the region's most trusted suppliers.
              </p>
              <p>
                Over the past two decades, we have built strong partnerships with leading manufacturers 
                worldwide, ensuring our clients have access to the latest technology and most reliable 
                equipment in the industry. Our commitment to excellence has earned us the trust of major 
                construction companies, mining operations, and industrial facilities across the Gulf.
              </p>
              <p>
                Today, we continue to evolve and adapt to meet the changing needs of our industry, 
                while maintaining the personal touch and dedication to service that has been our 
                hallmark since day one.
              </p>
            </div>
          </div>
          
          {/* Company Image Placeholder */}
          <div className="bg-gradient-to-br from-blue-100 to-blue-200 rounded-xl aspect-square flex items-center justify-center">
            <div className="text-center">
              <div className="w-24 h-24 bg-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <Wrench className="h-12 w-12 text-white" />
              </div>
              <h3 className="text-lg font-semibold text-gray-700 mb-2">Company Heritage</h3>
              <p className="text-gray-600 text-sm">
                25+ years of industrial excellence<br />
                serving the Gulf region
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Statistics */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
        {stats.map((stat, index) => (
          <div key={index} className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 text-center">
            <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-4">
              <stat.icon className="h-6 w-6 text-blue-600" />
            </div>
            <div className="text-3xl font-bold text-gray-900 mb-2">{stat.value}</div>
            <div className="text-sm text-gray-600">{stat.label}</div>
          </div>
        ))}
      </div>

      {/* Our Values */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8">
        <h2 className="text-3xl font-bold text-gray-900 text-center mb-8">Our Values</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {values.map((value, index) => (
            <div key={index} className="flex items-start space-x-4">
              <div className="flex-shrink-0">
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <value.icon className="h-6 w-6 text-blue-600" />
                </div>
              </div>
              <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">{value.title}</h3>
                <p className="text-gray-600">{value.description}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Leadership Team */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8">
        <h2 className="text-3xl font-bold text-gray-900 text-center mb-8">Leadership Team</h2>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {/* CEO */}
          <div className="text-center">
            <div className="w-32 h-32 bg-gradient-to-br from-blue-100 to-blue-200 rounded-full mx-auto mb-4 flex items-center justify-center">
              <div className="text-center">
                <Users className="h-12 w-12 text-blue-600 mx-auto mb-2" />
                <div className="text-xs text-gray-600">CEO Photo</div>
              </div>
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-1">Ahmed Al-Mahmood</h3>
            <p className="text-blue-600 font-medium mb-2">Chief Executive Officer</p>
            <p className="text-sm text-gray-600">
              Leading the company with 20+ years of experience in industrial equipment and business development.
            </p>
          </div>

          {/* CTO */}
          <div className="text-center">
            <div className="w-32 h-32 bg-gradient-to-br from-green-100 to-green-200 rounded-full mx-auto mb-4 flex items-center justify-center">
              <div className="text-center">
                <Wrench className="h-12 w-12 text-green-600 mx-auto mb-2" />
                <div className="text-xs text-gray-600">CTO Photo</div>
              </div>
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-1">Sarah Johnson</h3>
            <p className="text-green-600 font-medium mb-2">Chief Technical Officer</p>
            <p className="text-sm text-gray-600">
              Expert in drilling technology and equipment innovation with extensive field experience.
            </p>
          </div>

          {/* Operations Director */}
          <div className="text-center">
            <div className="w-32 h-32 bg-gradient-to-br from-purple-100 to-purple-200 rounded-full mx-auto mb-4 flex items-center justify-center">
              <div className="text-center">
                <Target className="h-12 w-12 text-purple-600 mx-auto mb-2" />
                <div className="text-xs text-gray-600">Director Photo</div>
              </div>
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-1">Mohammed Al-Rashid</h3>
            <p className="text-purple-600 font-medium mb-2">Operations Director</p>
            <p className="text-sm text-gray-600">
              Oversees daily operations and ensures seamless delivery of products and services to clients.
            </p>
          </div>
        </div>
      </div>

      {/* Mission & Vision */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-blue-50 border border-blue-200 rounded-xl p-8">
          <div className="flex items-center space-x-3 mb-4">
            <div className="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center">
              <Target className="h-5 w-5 text-white" />
            </div>
            <h3 className="text-2xl font-bold text-gray-900">Our Mission</h3>
          </div>
          <p className="text-gray-700 leading-relaxed">
            To provide the highest quality drilling and mining equipment, backed by exceptional service 
            and technical expertise, enabling our clients to achieve their operational goals safely and efficiently.
          </p>
        </div>

        <div className="bg-green-50 border border-green-200 rounded-xl p-8">
          <div className="flex items-center space-x-3 mb-4">
            <div className="w-10 h-10 bg-green-600 rounded-lg flex items-center justify-center">
              <Globe className="h-5 w-5 text-white" />
            </div>
            <h3 className="text-2xl font-bold text-gray-900">Our Vision</h3>
          </div>
          <p className="text-gray-700 leading-relaxed">
            To be the leading provider of industrial equipment solutions in the Gulf region, 
            recognized for innovation, reliability, and unwavering commitment to customer success.
          </p>
        </div>
      </div>

      {/* Certifications & Partnerships */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8">
        <h2 className="text-3xl font-bold text-gray-900 text-center mb-8">Certifications & Partnerships</h2>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="text-center">
            <div className="w-20 h-20 bg-yellow-100 rounded-lg flex items-center justify-center mx-auto mb-4">
              <Award className="h-10 w-10 text-yellow-600" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">ISO 9001:2015</h3>
            <p className="text-sm text-gray-600">Quality Management System certification ensuring consistent service delivery.</p>
          </div>

          <div className="text-center">
            <div className="w-20 h-20 bg-red-100 rounded-lg flex items-center justify-center mx-auto mb-4">
              <Shield className="h-10 w-10 text-red-600" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Safety Standards</h3>
            <p className="text-sm text-gray-600">Compliance with international safety standards and regulations.</p>
          </div>

          <div className="text-center">
            <div className="w-20 h-20 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-4">
              <Globe className="h-10 w-10 text-blue-600" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Global Partners</h3>
            <p className="text-sm text-gray-600">Authorized distributor for leading international equipment manufacturers.</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AboutUs;